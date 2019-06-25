import {NotificationType}   from "../components/main/panel/rightpanel/notification/Notification";
import {EntityType}         from "./entity/EntityType";

export type Notification = {
    key: string,
    type: NotificationType,
    message: string,
    targetType: EntityType,
    folderName: string,
    processing: boolean
}