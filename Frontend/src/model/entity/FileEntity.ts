import {Entity} from "./Entity";

export interface FileEntity extends Entity {
    size: string;
	timeCreated: string
	fileType: string
}
