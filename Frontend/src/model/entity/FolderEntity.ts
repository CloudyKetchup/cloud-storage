import {Entity} from "./Entity";

export interface FolderEntity extends Entity {
    timeCreated: string | undefined
    parentId: number | null
    root: boolean
}