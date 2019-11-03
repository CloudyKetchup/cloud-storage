import { Entity } from "./Entity";
import { MediaEntity } from "./MediaEntity";

export interface FileEntity extends Entity {
	parentId : string
	extension: string
	image 	 : MediaEntity | null
}
