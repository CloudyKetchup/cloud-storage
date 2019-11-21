import { Component } from "react";

import { ContentContextInterface }	from './context/ContentContext';
import { Entity }					from './model/entity/Entity';
import { FileEntity } 				from './model/entity/FileEntity';
import { FileExtension } 			from './model/entity/FileExtension';
import { FolderEntity } 			from './model/entity/FolderEntity';
import { NotificationEntity }		from './model/notification/NotificationEntity';
import { NotificationType } 		from './model/notification/NotificationType';
import { ProcessingContext } 		from "./context/ProcessingContext";
import { UploadItem } 				from './model/UploadItem';
import NavNode						from './model/NavNode';
import { UploadingContextInterface }from './context/UploadingContext';
import { UploadingContextImpl }		from './components/UploadingPane/UploadingPaneComponent';
import UploadQueue 					from './utils/UploadQueue';
import { ErrorNotificationEntity, ErrorNotificationType } 	from './model/notification/ErrorNotificationEntity';
import App, {AppContentContext, AppNotificationContext } 	from './App';
import { NotificationsContextInterface } from './context/NotificationContext';

import axios, { CancelToken } from 'axios';

export const API_URL = 'http://localhost:8080';

export class APIHelpers {

	static getRootId = () : Promise<string> => (
		axios.get(`${API_URL}/folder/root/id`)
			.then(id => id.data)
	);

	static getRootData = () : Promise<FolderEntity> => (
		axios.get(`${API_URL}/folder/root/data`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error fetching home folder data"))
	);

	static getRootMemory = () : Promise<object> => (
		axios.get(`${API_URL}/folder/root/memory`)
			.then(memory => memory.data)
			.catch(() => APIHelpers.errorNotification("Error fetching memory usage"))
	);

	static getFolderPredecessors = (id : string) : Promise<FolderEntity[]> => (
		axios.get(`${API_URL}/folder/${id}/predecessors`)
			.then(response => response.data)
	);

	static getFileData = (id: string) : Promise<FileEntity> => (
		axios.get(`${API_URL}/file/${id}/data`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error fetching file data"))
	);
 
	static getFolderData = (id: string) : Promise<FolderEntity> => (
		axios.get(`${API_URL}/folder/${id}/data`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error fetching folder data"))
	);

	static getFolderFiles = (id: string) : Promise<[]> => (
		axios.get(`${API_URL}/folder/${id}/files`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error fetching folder files"))
	);

	static getFolderFolders = (id: string) : Promise<[]> => (
		axios.get(`${API_URL}/folder/${id}/folders`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error fetching folder folders"))
	);

	static getTrashItems = (): Promise<[]> => (
		axios.get(`${API_URL}/trash/items`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error fetching items from trash"))
	);

	static folderHasContent = (id: string): Promise<boolean> => (
		axios.get(`${API_URL}/folder/${id}/content_info`)
			.then(response => response.data.folderCount > 0 || response.data.filesCount > 0)
			.catch(() => false)
	);

	static zipFolder = (folderPath: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/zip`,
			{
				path: folderPath
			})
			.then(response => response.data)
	);

	static createNewFolder = (name: string, path: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/create/`,
			{
				name		: name,
				folderPath	: path
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error creating folder ${name}`))
	);

	static pasteEntity = (target: Entity, action: string, newPath: string) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/${action}`,
			{
				oldPath: target.path,
				newPath: newPath
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error when pasting ${target.name}`))
	);

	static moveToTrash = (target: Entity) : Promise<string> => (
		axios.post(`${API_URL}/trash/${target.type.toLowerCase()}/move-to-trash`,
			{
				id : target.id
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error moving to trash ${target.name}`))
	);

	static restoreFromTrash = (id : string) : Promise<string> => (
		axios.post(`${API_URL}/trash/restore-from-trash`,
			{
				id : id
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error restoring from trash"))
	);

	static restoreAllFromTrash = () : Promise<string> => (
		axios.post(`${API_URL}/trash/restore-all`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error restoring all trash items"))
	);

	static deleteFromTrash = (target: Entity) : Promise<string> => (
		axios.delete(`${API_URL}/trash/delete/${target.id}`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error deleting from trash ${target.name}`))
	);

	static emptyTrash = () : Promise<string> => (
		axios.delete(`${API_URL}/trash/empty-trash`)
			.then(response => response.data)
			.catch(APIHelpers.errorNotification)
	);

	static renameEntity = (target: Entity, newName: string) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/rename`,
			{
				'id': target.id,
				'newName': newName
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error renaming ${target.name} to ${newName}`))
	);

	static deleteEntity = (target: Entity) : Promise<string> => (
		axios.delete(`${API_URL}/${target.type.toLowerCase()}/${target.id}/delete`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error deleting ${target.name}`))
	);

	static deleteFolderContent = (id : string) : Promise<string> => (
		axios.delete(`${API_URL}/folder/${id}/delete-all`)
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error deleting all folder content"))
	);

	static uploadFile = async (file : File, folder : FolderEntity, uploadItem? : UploadItem, cancelToken? : CancelToken) => {
		const URL = `${API_URL}/file/upload/one`;
		const formData = new FormData();

		formData.append('file', file);
		formData.append('path', folder.path);

		await axios({
				url     : URL,
				cancelToken : cancelToken,
				method  : 'POST',
				data    : formData,
				onUploadProgress : p => {
					if (uploadItem) uploadItem.progress = `${(p.total - (p.total - p.loaded)) / p.total * 100}`;
				}
			})
			.then(response => {
				response.data !== "OK" && APIHelpers.errorNotification("Error uploading");

				AppContentContext.currentFolder.id === folder.id
				&&
				uploadItem
				&&
				!UploadQueue.getInstance().jobCanceled(uploadItem.id)
				&&
				response.data === "OK"
				&&
				ContentHelpers.updateFiles(folder.id);
			})
			.catch(e => !axios.isCancel(e) && APIHelpers.errorNotification("Error starting upload"));
	};

	static uploadFiles = (folder : FolderEntity, files : File[]) => {
		files.forEach(async file => {
			const uploadItem = UploadHelpers.createUploadItem(file, folder);;

			UploadingContextImpl.addUpload(uploadItem);
			
			const queue = UploadQueue.getInstance();

			queue.add(queue.createJob(uploadItem));
		});
	};

	static downloadFile = async (path: string, name: string) => {
		const link = document.createElement("a");
		
		link.download = name;

		link.href = `${API_URL}/file/${path.replace(/[/]/g, '%2F')}/download`;

		document.body.appendChild(link);
		
		link.click();
	};

	static downloadFolder = async (target: FolderEntity) => {
		if (await APIHelpers.folderHasContent(target.id)) {
			const zip = await APIHelpers.zipFolder(target.path);

			if (zip !== "folder is empty") await APIHelpers.downloadFile(zip, target.name);
		} else APIHelpers.errorNotification(`Folder "${target.name}" is empty`);
	};

	static errorNotification = (message? : string) => {
		const text = message || "API call error :(";

		AppNotificationContext
		&&
		AppNotificationContext.add(NotificationHelpers.createErrorNotification(text, ErrorNotificationType.ERROR));
	};
}

export class ContentHelpers {

	static updateFiles = async (folderId : string) =>  AppContentContext.setFiles(await APIHelpers.getFolderFiles(folderId));

	static updateFolders = async (folderId : string) => AppContentContext.setFolders(await APIHelpers.getFolderFolders(folderId));

	static updateTrash = async () => AppContentContext.setTrashItems(await APIHelpers.getTrashItems());

	static updateContent = async (folderId : string) : Promise<Boolean> => {
		if (folderId === undefined) return false;

		await ContentHelpers.updateFiles(folderId);
		await ContentHelpers.updateFolders(folderId);

		return true;
	};
}

export class NavigationNodesHelpers {

	static createNavNode = (entity: FolderEntity, prevNode: NavNode | undefined, app: App) => {
		return new NavNode(
			entity.id,
			entity.name,
			prevNode,
			async () => app.updateFolder(entity.id)
				.then(async () => await NavigationNodesHelpers.removeNodeSuccessors(entity.id, app)));
	};

	static removeNodeSuccessors = async (id: string, app: App) => {
		const nodes = app.state.foldersNavigation;

		nodes.splice(nodes.findIndex(node => node.id === id) + 1, 9e9);

		app.setState({ foldersNavigation : nodes });
	};
}

export class EntityHelpers {

	static uuidv4 = () => 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
		const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r ? 0x3 : 0x8);
		return v.toString(16);
	});
}

export class NotificationHelpers {

	static createErrorNotification = (text : string, errorType: ErrorNotificationType) : ErrorNotificationEntity => {
		return {
			id 			: EntityHelpers.uuidv4(),
			text		: text,
			errorType	: errorType,
			type 		: NotificationType.ERROR
		};
	};
}

export class ContextHelpers {

	static createContentContext = (currentFolder : FolderEntity, component? : Component) : ContentContextInterface => {
		return new class {
			files 		: FileEntity[]	 = [];
			folders 	: FolderEntity[] = [];
			trashItems 	: Entity[]		 = [];
			currentFolder : FolderEntity = currentFolder;

			setFiles = (files : FileEntity[] = []) => {
				this.files = files;

				component && component.forceUpdate();

				return this.files;
			};

			setFolders = (folders : FolderEntity[] = []) => {
				this.folders = folders;

				component && component.forceUpdate();

				return this.folders;
			};

			setTrashItems = (items : Entity[] = []) => {
				this.trashItems = items;

				component && component.forceUpdate();

				return this.trashItems;
			};

			setCurrentFolder = (folder : FolderEntity) => {
				this.currentFolder = folder;

				component && component.forceUpdate();
			}
		}();
	};

	static createNotificationContext = (component? : Component) : NotificationsContextInterface => {
		return new class implements NotificationsContextInterface {
			notifications : NotificationEntity[] = [];

			add = (notification : NotificationEntity) => {
				this.notifications.push(notification);

				component && component.forceUpdate();

				return this.notifications;
			};

			delete = (id : string) => {
				const index = this.notifications.findIndex(n => n.id === id);

				this.notifications.splice(index, 1);

				component && component.forceUpdate();
			};
		}();
	};

	static createProcessingContext = (component? : Component) : ProcessingContext => {
		return new class implements ProcessingContext {
			entities : Entity[] = [];

			add = (entity : Entity) => {
				this.entities.push(entity);

				component && component.forceUpdate();
			};

			get = (id : string) : Entity | null => this.entities.filter(i => i.id === id)[0];

			delete = (id : string) => this.entities.splice(this.entities.findIndex(i => i.id === id), 1);
		}();
	};

	static createUploadContext = (component? : Component) : UploadingContextInterface => {
		return new class implements UploadingContextInterface {
			uploads : UploadItem[] = [];

			addUpload = (item : UploadItem) : UploadItem => {
				this.uploads.push(item);

				component && component.setState({ items : this.uploads });

				return item;
			};

			deleteUpload = (id : string) => {
				this.uploads.splice(this.uploads.findIndex(i => i.id === id), 1);

				component && component.setState({ items : this.uploads });
			};

			clearAllUploads = async () => {
				await UploadQueue.getInstance().selfDestroy();

				this.uploads = [];

				component && component.setState({ items : this.uploads });
			};

			updateComponent = () => component && component.forceUpdate();
		}();
	};
}

export class UploadHelpers {
	
	static createUploadItem = (file : File, folder : FolderEntity) : UploadItem => {
		return {
			id : EntityHelpers.uuidv4(),
			file : file,
			folder : folder,
			progress : "0"
		};
	};
}

export class FileHelpers {

	static imageAssign = (data : FileEntity) : boolean => {
	    const ext = FileExtension;

		switch (data.extension) {
			case ext.IMAGE_JPEG:
			case ext.IMAGE_JPG:
			case ext.IMAGE_GIF:
			case ext.IMAGE_PNG:
			case ext.IMAGE_RAW:
				return true;
			default: return false;
		}
	};
}
