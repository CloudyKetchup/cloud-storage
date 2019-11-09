import { NotificationEntity } from "./NotificationEntity";

export enum ErrorNotificationType {
    ERROR = "Error",
    WARNING = "Warning"
}

export interface ErrorNotificationEntity extends NotificationEntity {
    errorType   : ErrorNotificationType
}