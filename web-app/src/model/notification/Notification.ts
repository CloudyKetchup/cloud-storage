import {NotificationType}   from "../../components/Notification/Notification";
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