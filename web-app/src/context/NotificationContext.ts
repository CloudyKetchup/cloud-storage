import { NotificationEntity } from "../model/notification/NotificationEntity";

export interface NotificationsContextInterface {
    notifications	: NotificationEntity[]
    add				: (notifications : NotificationEntity) => NotificationEntity[]
    delete			: (id : string) => void
}
