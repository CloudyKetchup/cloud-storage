import { UploadItem } from '../model/UploadItem';

export interface UploadingContextInterface {
	uploads : UploadItem[]
	addUpload : (item : UploadItem) => void
	deleteUpload : (id : string) => void
	clearAllUploads? : () => void
	updateComponent? : () => void
}
