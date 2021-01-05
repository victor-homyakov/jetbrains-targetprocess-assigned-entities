package com.targetprocess.entities.assigned;

import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * api/v1 query examples:
 *
 * ?include=%5Bid,name,description,createDate,modifyDate,entityType,entityState%5D
 * &where=(assignedUser.id+eq+50916)+and+(entityState.isFinal+eq+'false')&orderByDesc=modifyDate&take={max}&skip={since}
 *
 * &where=(PlannedEndDate%20is%20not%20null)%20and%20(EntityState.IsFinal%20eq%20%22false%22)
 *
 * &take=100&include=[PlannedEndDate,Id,Project[Process[Name]]]
 * &include=[Description,Owner,CreateDate,General[EntityType,Project[Color,Abbreviation],Name]]
 *
 * api/v2 query examples:
 *
 * https://test.tpondemand.com/api/v2/assignable
 * ?select={id,name,description,entityType:entityType.name,entityState:{entityState.id,entityState.name,entityState.isFinal}}
 * &where=(assignedUser.where(it.login=='admin').Count>0)and(entityState.isFinal==false)and(name.contains('code'))
 * and(entityType.name=='Bug'%20or%20entityType.name=='UserStory')
 * &orderBy=id desc
 *
 * pagination:
 * https://test.tpondemand.com/api/v2/assignable
 * ?where=(assignedUser.where(it.login=='admin').Count>0)and(entityType.name=='Task')and(entityState.isFinal==false)
 * &select={id,name,description,entityType:entityType.name}
 * &take=25&skip=25
 * &orderBy=id desc
 */
@Tag("Targetprocess")
public class TargetprocessRepository extends NewBaseRepositoryImpl {

    private static final Logger LOG = Logger.getInstance(TargetprocessRepository.class);
    public static final String API_PATH = "/api/v2/assignable";

    /**
     * Serialization constructor
     */
    @SuppressWarnings("UnusedDeclaration")
    public TargetprocessRepository() {
    }

    public TargetprocessRepository(TaskRepositoryType type) {
        super(type);
        setUseHttpAuthentication(true);
    }

    public TargetprocessRepository(TargetprocessRepository repository) {
        super(repository);
    }

    public static String getRequestUrl(String serverUrl, String userName, @Nullable String query) {
        List<String> where = new ArrayList<>();
        where.add("(assignedUser.where(it.login=='" + userName + "').Count>0)");
        where.add("(entityType.name=='Bug' or entityType.name=='UserStory')");
        where.add("(entityState.isFinal==false)");
        if (StringUtil.isNotEmpty(query)) {
            where.add("(name.contains('" + query + "') or id.ToString().contains('" + query + "'))");
        }

        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(serverUrl + API_PATH);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        uriBuilder.addParameter("select", "{" + Assignable.FIELDS + "}");
        uriBuilder.addParameter("where", String.join("and", where));
        uriBuilder.addParameter("orderBy", "id desc");

        return uriBuilder.toString();
    }

    @NotNull
    public String getRequestUrl(@Nullable String query) {
        return getRequestUrl(getUrl(), getUsername(), query);
    }

    //TODO use withClosed parameter in url
    //TODO use offset and limit: &take=30&skip=30
    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed,
            @NotNull ProgressIndicator cancelled) throws Exception {
        Assignable.serverUrl = getUrl();
        String requestUrl = getRequestUrl(query);
        LOG.info(String.format("Get issues: offset %d limit %d query %s", offset, limit, requestUrl));

        HttpClient client = getHttpClient();
        HttpGet request = new HttpGet(requestUrl);

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseString = client.execute(request, responseHandler);
            Gson gson = new Gson();
            AssignablesWrapper response = gson.fromJson(responseString, AssignablesWrapper.class);
            LOG.info("Received " + response.getItems().length + " issues");
            return response.getItems();
        } catch (Exception e) {
            LOG.error(String.format("Cannot get response body for request %s", requestUrl), e);
            throw e;
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Return server's REST API path prefix, e.g. {@code /rest/api/latest} for JIRA or {@code /api/v3} for Gitlab.
     * This value will be used in {@link #getRestApiUrl(Object...)}
     *
     * @return server's REST API path prefix
     */
    @Override
    @NotNull
    public String getRestApiPathPrefix() {
        return API_PATH;
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        return new HttpTestConnection(new HttpGet(getRestApiUrl()));
    }

    @Nullable
    @Override
    public Task findTask(@NotNull String id) throws Exception {
        LOG.info(String.format("Find task: id '%s' (not implemented)", id));
        return null;
    }

    @NotNull
    @Override
    public BaseRepository clone() {
        return new TargetprocessRepository(this);
    }

    @Override
    protected int getFeatures() {
        return super.getFeatures() | NATIVE_SEARCH;
    }

}
