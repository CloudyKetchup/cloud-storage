import {EntityType} from "./EntityType";

export type Entity = {
    id: string
    name: string
    path: string
    location: string | null
    timeCreated: string | undefined
    size: string;
	type: EntityType
}
