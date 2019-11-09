import { NotificationEntity } from "../model/notification/NotificationEntity";

export interface NotificationsContextInterface {
    notifications    : NotificationEntity[]
    setNotifications : (notifications : NotificationEntity[]) => NotificationEntity[]
    deleteNotification : (id : string) => void
}