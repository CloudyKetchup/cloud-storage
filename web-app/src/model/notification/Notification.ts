import {NotificationType}   from "../../components/main/panel/rightpanel/notification/Notification";
import {EntityType}         from "../entity/EntityType";

export type Notification = {
    key 		: number,
    type 		: NotificationType,
    message 	: string,
    targetType 	: EntityType,
    name 		: string,
    processing 	: boolean,
    error 		: boolean
}