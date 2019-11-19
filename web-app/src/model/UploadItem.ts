import { FolderEntity } from './entity/FolderEntity';

export interface UploadItem {
	id : string
	file : File
	folder: FolderEntity
	progress : string
}
