import { Entity }		 from "./Entity";
import { MediaEntity }	 from "./MediaEntity";
import { FileExtension } from "./FileExtension";

export interface FileEntity extends Entity {
	parentId : string
	extension: FileExtension
	image 	 : MediaEntity | null
}
