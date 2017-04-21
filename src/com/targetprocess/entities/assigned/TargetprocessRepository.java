package com.targetprocess.entities.assigned;

import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.CustomTaskState;
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

import java.net.URI;
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
 *
 * get entity by id:
 * https://test.tpondemand.com/api/v2/assignable
 * ?select={id,name,description,entityType:entityType.name,entityState:{entityState.id,entityState.name,entityState.isFinal}}
 * &where=(id==123456)and(entityType.name=='Bug'%20or%20entityType.name=='UserStory')
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

    public static URIBuilder getRequestUrl(String serverUrl, String userName, @Nullable String query,
        int offset, int limit, boolean withClosed) {

        List<String> where = buildWhereParameter(userName);
        if (!withClosed) {
            where.add("(entityState.isFinal==false)");
        }
        if (StringUtil.isNotEmpty(query)) {
            where.add("(name.contains('" + query + "') or id.ToString().contains('" + query + "'))");
        }

        URIBuilder url = buildUrl(serverUrl, where, "id desc");

        if (offset != 0 || limit != 0) {
            url.addParameter("take", String.valueOf(limit));
            url.addParameter("skip", String.valueOf(offset));
        }

        return url;
    }

    @NotNull
    public static URIBuilder getRequestUrl(String serverUrl, String userName, @NotNull String id) {
        List<String> where = buildWhereParameter(userName);
        where.add("(id==" + id + ")");

        return buildUrl(serverUrl, where, null);
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed,
        @NotNull ProgressIndicator cancelled) throws Exception {
        LOG.info(String.format("Get entities: offset %d limit %d withClosed %s", offset, limit, withClosed));
        Assignable.serverUrl = getUrl();
        URI requestUrl = getRequestUrl(getUrl(), getUsername(), query, offset, limit, withClosed).build();
        return getIssues(requestUrl);
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
        LOG.info("Find entity with id '" + id + "'");
        if (StringUtil.isEmpty(id)) {
            return null;
        }

        URI requestUrl = getRequestUrl(getUrl(), getUsername(), id).build();
        Task[] tasks = getIssues(requestUrl);
        switch (tasks.length) {
            case 0:
                return null;
            case 1:
                return tasks[0];
            default:
                LOG.warn("Expected unique entity for id '" + id +
                    "', got " + tasks.length + " instead. Using the first one.");
                return tasks[0];
        }
    }

    /**
     * <p>
     * Attempts to extract server ID of the issue from the ID of local task (probably restored from project settings).
     * Returns the ID of local task if it is a number (Targetprocess entities have numeric IDs), null otherwise.
     * </p>
     * <p>
     * Basically this method works as filter that tells what repository local task belongs to. If it returns not {@code null},
     * this repository is attached to that local task and is used then to refresh it via {@link #findTask(String)},
     * update its state via {@link #setTaskState(Task, CustomTaskState)}, etc. Because the decision is based only on syntactic
     * structure of ID, this approach works poorly in case of several repositories with similar issue IDs, e.g. JIRA and YouTrack,
     * and so it's a subject of change in future.
     * </p>
     * See {@link com.intellij.tasks.TaskRepository#extractId(String)} for full description.
     *
     * @param taskName ID of the task to check
     *
     * @return extracted ID of the issue or {@code null} if it doesn't look as issue ID of this tracker
     */
    @Nullable
    @Override
    public String extractId(@NotNull String taskName) {
        String id = TaskIdConverter.extractId(taskName);
        LOG.info("Extract id: entity '" + taskName + "' -> id '" + id + "'");
        return id;
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

    private static List<String> buildWhereParameter(@NotNull String userName) {
        List<String> where = new ArrayList<>();
        where.add("(assignedUser.where(it.login=='" + userName + "').Count>0)");
        where.add("(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')");
        return where;
    }

    private static URIBuilder buildUrl(String serverUrl, List<String> where, String orderBy) {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(serverUrl + API_PATH);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        uriBuilder.addParameter("select", "{" + Assignable.FIELDS + "}");
        uriBuilder.addParameter("where", String.join("and", where));
        if (StringUtil.isNotEmpty(orderBy)) {
            uriBuilder.addParameter("orderBy", orderBy);
        }

        return uriBuilder;
    }

    private Task[] getIssues(URI requestUrl) throws Exception {
        LOG.info(String.format("Get entities %s", requestUrl));

        HttpClient client = getHttpClient();
        HttpGet request = new HttpGet(requestUrl);

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseString = client.execute(request, responseHandler);
            Gson gson = new Gson();
            AssignablesWrapper response = gson.fromJson(responseString, AssignablesWrapper.class);
            Assignable[] assignables = response.getItems();
            LOG.info("Received " + assignables.length + " entities");
            return assignables;
        } catch (Exception e) {
            LOG.error(String.format("Cannot get response body for request %s", requestUrl), e);
            throw e;
        } finally {
            request.releaseConnection();
        }
    }
}
