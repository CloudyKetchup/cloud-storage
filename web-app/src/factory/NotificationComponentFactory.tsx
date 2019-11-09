import React from "react";

import {NotificationEntity} from "../model/notification/NotificationEntity";
import Notification         from "../components/Notification/Notification";
import {NotificationType}   from "../model/notification/NotificationType";
import ErrorNotification    from "../components/Notification/ErrorNotification/ErrorNotification";

export default class NotificationComponentFactory {

    static build = (data : NotificationEntity) => {
        switch (data.type) {
            case NotificationType.BASIC:
                return <Notification id={`notification-${data.id}`} key={data.id} data={data} autoDestroy expandable/>;
            case NotificationType.ERROR:
                return <ErrorNotification id={`notification-${data.id}`} key={data.id} data={data} autoDestroy expandable/>;
            default: return undefined;
        }
    };
}