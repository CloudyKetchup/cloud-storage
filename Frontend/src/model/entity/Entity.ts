import {EntityType} from "./EntityType";

export type Entity = {
    id: number
    name: string
    path: string
    location: string | null
    type: EntityType
}