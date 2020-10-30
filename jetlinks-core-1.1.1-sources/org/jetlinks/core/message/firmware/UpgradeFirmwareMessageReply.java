package org.jetlinks.core.message.firmware;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.message.CommonDeviceMessageReply;
import org.jetlinks.core.message.MessageType;

/**
 * 固件更新回复
 *
 * @author zhouhao
 * @since 1.0.3
 */
@Getter
@Setter
public class UpgradeFirmwareMessageReply extends CommonDeviceMessageReply<UpgradeFirmwareMessageReply> {

    @Override
    public MessageType getMessageType() {
        return MessageType.UPGRADE_FIRMWARE_REPLY;
    }
}
