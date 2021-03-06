package team.catgirl.vox.client.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import okhttp3.*;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.api.http.ChannelService;

import java.io.IOException;

public final class ChannelServiceClient implements ChannelService {

    private static final ObjectMapper MAPPER = new JsonMapper();

    private final OkHttpClient http;
    private final String baseUrl;

    public ChannelServiceClient(OkHttpClient http, String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    @Override
    public PermitAccessResponse permit(PermitAccessRequest req) {
        return httpPost("/api/1/channels/permit/accept", req, PermitAccessResponse.class);
    }

    @Override
    public DenyAccessResponse deny(DenyAccessRequest req) {
        return httpPost("/api/1/channels/permit/deny", req, DenyAccessResponse.class);
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller, byte[] permit) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller) {
        throw new RuntimeException("not implemented");
    }

    private <T> T httpPost(String url, Object post, Class<T> tClass) {
        byte[] bytes;
        try {
            bytes = MAPPER.writeValueAsBytes(post);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(RequestBody.create(bytes, MediaType.get("application/json")))
                .build();

        try (Response response = http.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new RuntimeException("bad response");
            }
            return MAPPER.readValue(response.body().bytes(), tClass);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
