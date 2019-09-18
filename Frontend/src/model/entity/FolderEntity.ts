import {Entity} from "./Entity";

export interface FolderEntity extends Entity {
    parentId: string | null
    root: boolean
}