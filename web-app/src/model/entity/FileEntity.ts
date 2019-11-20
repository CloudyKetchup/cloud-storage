import { Entity }		 from "./Entity";
import { FileExtension } from "./FileExtension";

export interface FileEntity extends Entity {
	parentId : string
	extension: FileExtension
	isMedia  : boolean
}
