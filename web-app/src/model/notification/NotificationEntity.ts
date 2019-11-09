import { NotificationType } from "./NotificationType";

export interface NotificationEntity {
    id  : string
    type: NotificationType
    text: string
}