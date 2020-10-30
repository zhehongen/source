package org.jetlinks.core.message.firmware;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.message.CommonDeviceMessage;
import org.jetlinks.core.message.MessageType;

import java.util.Map;

/**
 * 设备上报固件信息
 *
 * @since 1.0.3
 * @author zhouhao
 */
@Getter
@Setter
public class ReportFirmwareMessage extends CommonDeviceMessage {

    //版本号
    private String version;

    //其他属性
    private Map<String, Object> properties;

    @Override
    public MessageType getMessageType() {
        return MessageType.REPORT_FIRMWARE;
    }
}
