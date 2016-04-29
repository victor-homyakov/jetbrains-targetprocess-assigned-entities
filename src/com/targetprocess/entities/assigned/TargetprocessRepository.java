package com.targetprocess.entities.assigned;

import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.BaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author khomyackov
 *         TODO inherit from NewBaseRepositoryImpl
 */
@Tag("Targetprocess")
public class TargetprocessRepository extends BaseRepositoryImpl {

    private static final Logger LOG = Logger.getInstance(TargetprocessRepository.class);

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
        // api/v1:

        // ?include=%5Bid,name,description,createDate,modifyDate,entityType,entityState%5D
        // &where=(assignedUser.id+eq+50916)+and+(entityState.isFinal+eq+'false')&orderByDesc=modifyDate&take={max}&skip={since}

        // &where=(PlannedEndDate%20is%20not%20null)%20and%20(EntityState.IsFinal%20eq%20%22false%22)

        // &take=100&include=[PlannedEndDate,Id,Project[Process[Name]]]
        // &include=[Description,Owner,CreateDate,General[EntityType,Project[Color,Abbreviation],Name]]

        // api/v2:

        // https://plan.tpondemand.com/api/v2/assignable
        // ?select={id,name,description,entityType:entityType.name,entityState:{entityState.id,entityState.name,entityState.isFinal}}
        // &where=(assignedUser.where(it.login=='admin').Count>0)and(entityState.isFinal==false)and(name.contains('code'))
        // and(entityType.name=='Bug'%20or%20entityType.name=='UserStory')
        // &orderBy=id desc

        List<String> where = new ArrayList<>();
        where.add("(assignedUser.where(it.login=='" + userName + "').Count>0)");
        where.add("(entityType.name=='Bug' or entityType.name=='UserStory')");
        where.add("(entityState.isFinal==false)");
        if (StringUtil.isNotEmpty(query)) {
            where.add("(name.contains('" + query + "') or id.ToString().contains('" + query + "'))");
        }

        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(serverUrl + "/api/v2/assignable");
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
            /*@NotNull*/ @Nullable ProgressIndicator cancelled) throws Exception {
        Assignable.serverUrl = getUrl();
        String requestUrl = getRequestUrl(query);
        LOG.info(String.format("Get issues %s", requestUrl));

        HttpClient client = getHttpClient();
        GetMethod method = new GetMethod(requestUrl);
        client.executeMethod(method);

        try {
            String responseString = method.getResponseBodyAsString();
            Gson gson = new Gson();
            AssignablesWrapper response = gson.fromJson(responseString, AssignablesWrapper.class);
            LOG.info("Received " + response.getItems().length + " assignable items");
            return response.getItems();
        } catch (Exception e) {
            LOG.error(String.format("Cannot get response body for request %s", requestUrl), e);
            throw e;
        } finally {
            method.releaseConnection();
        }
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        //return new NewBaseRepositoryImpl.HttpTestConnection(new HttpGet(getRequestUrl("")));
        return new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                getIssues("", 0, 1, false, null);
            }

            @Override
            public void cancel() {
            }
        };
    }

    @Nullable
    @Override
    public Task findTask(@NotNull String id) throws Exception {
        return null;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
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
