import React        from "react";
import Notification, {NotificationProps} from "../Notification";

export default class ErrorNotification extends Notification<NotificationProps> {

    icon = (icon: JSX.Element, style?: {}) => <div style={style}>{icon}</div>;

    render = () => (
        <Notification id={this.props.id} data={this.props.data} expandable>
            {this.icon(<i style={{ width : "10%" , color : "#F32C2C" }} className="fas fa-exclamation-triangle"/>)}
        </Notification>
    );
}