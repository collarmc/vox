package team.catgirl.vox.api.http;

import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;

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
        public final Channel channel;
        public final Caller caller;

        public PermitAccessRequest(Channel channel, Caller caller) {
            this.channel = channel;
            this.caller = caller;
        }
    }

    final class PermitAccessResponse {
        public final byte[] token;

        public PermitAccessResponse(byte[] token) {
            this.token = token;
        }
    }

    final class DenyAccessRequest {
        public final Channel channel;
        public final Caller caller;

        public DenyAccessRequest(Channel channel, Caller caller) {
            this.channel = channel;
            this.caller = caller;
        }
    }

    final class DenyAccessResponse {}
}
