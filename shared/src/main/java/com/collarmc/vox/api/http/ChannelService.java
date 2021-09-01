package com.collarmc.vox.api.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;

public interface ChannelService {

    /**
     * @param req to allow a caller access to a channel
     * @return response
     */
    PermitAccessResponse permit(PermitAccessRequest req);

    /**
     * @param req to deny a caller access to a channel
     * @return response
     */
    DenyAccessResponse deny(DenyAccessRequest req);

    /**
     * Check if caller has a permit for channel
     * @param channel to test
     * @param caller to test
     * @param permit to confirm
     * @return permitted or not
     */
    boolean isPermitted(Channel channel, Caller caller, byte[] permit);

    /**
     * Check if caller has a permit for channel
     * @param channel to test
     * @param caller to test
     * @return permitted or not
     */
    boolean isPermitted(Channel channel, Caller caller);

    final class PermitAccessRequest {
        @JsonProperty("channel")
        public final Channel channel;
        @JsonProperty("caller")
        public final Caller caller;

        public PermitAccessRequest(@JsonProperty("channel") Channel channel,
                                   @JsonProperty("caller")Caller caller) {
            this.channel = channel;
            this.caller = caller;
        }
    }

    final class PermitAccessResponse {
        @JsonProperty("token")
        public final byte[] token;

        public PermitAccessResponse(@JsonProperty("token") byte[] token) {
            this.token = token;
        }
    }

    final class DenyAccessRequest {
        @JsonProperty("channel")
        public final Channel channel;
        @JsonProperty("caller")
        public final Caller caller;

        public DenyAccessRequest(@JsonProperty("channel") Channel channel,
                                 @JsonProperty("caller") Caller caller) {
            this.channel = channel;
            this.caller = caller;
        }
    }

    final class DenyAccessResponse {}
}
