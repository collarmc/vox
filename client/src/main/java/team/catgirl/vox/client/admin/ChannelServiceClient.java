package team.catgirl.vox.client.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.mikael.urlbuilder.UrlBuilder;
import okhttp3.*;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.api.http.ChannelService;
import team.catgirl.vox.utils.Utils;

import java.io.IOException;
import java.net.URL;

/**
 * Client API for accessing the channel service
 */
public final class ChannelServiceClient implements ChannelService {

    private final OkHttpClient http;
    private final UrlBuilder baseUrl;
    private final String password;

    public ChannelServiceClient(OkHttpClient http, String baseUrl, String password) {
        this.http = http;
        this.baseUrl = UrlBuilder.fromString(baseUrl);
        this.password = password;
    }

    @Override
    public PermitAccessResponse permit(PermitAccessRequest req) {
        return httpPost("api/1/channels/permit/accept", req, PermitAccessResponse.class);
    }

    @Override
    public DenyAccessResponse deny(DenyAccessRequest req) {
        return httpPost("api/1/channels/permit/deny", req, DenyAccessResponse.class);
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller, byte[] permit) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller) {
        throw new RuntimeException("not implemented");
    }

    private <T> T httpPost(String path, Object post, Class<T> tClass) {
        byte[] bytes;
        try {
            bytes = Utils.jsonMapper().writeValueAsBytes(post);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        URL url = baseUrl.withPath(path).toUrl();
        System.out.println(url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer" + password)
                .post(RequestBody.create(bytes, MediaType.get("application/json")))
                .build();

        try (Response response = http.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new RuntimeException("bad response " + response.code());
            }
            return Utils.jsonMapper().readValue(response.body().bytes(), tClass);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
