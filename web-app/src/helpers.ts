import { FolderEntity } 		from './model/entity/FolderEntity';
import { Entity } 				from './model/entity/Entity';
import NavNode					from './model/NavNode';
import { FileEntity } 			from './model/entity/FileEntity';
import { NotificationType } 	from './model/notification/NotificationType';
import { NotificationEntity }	from './model/notification/NotificationEntity';
import { ErrorNotificationEntity, ErrorNotificationType } from './model/notification/ErrorNotificationEntity';
import App, {AppContentContext, AppNotificationContext, AppProcessingContext} from './App';

import { ContentContextInterface }		 from './context/ContentContext';
import { NotificationsContextInterface } from './context/NotificationContext';

import axios from 'axios';
import {ProcessingContext} from "./context/ProcessingContext";

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

	static restoreFromTrash = (target: Entity) : Promise<string> => (
		axios.post(`${API_URL}/trash/restore-from-trash`,
			{
				id : target.id
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification(`Error restoring from trash ${target.name}`))
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

	static deleteFolderContent = (path: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/delete-all`,
			{
				path: path
			})
			.then(response => response.data)
			.catch(() => APIHelpers.errorNotification("Error deleting all folder content"))
	);

	private static uploadFile = async (file: File, appContext: App) => {
		const URL = `${API_URL}/file/upload/one`;

		const formData = new FormData();

		formData.append('file', file);
		formData.append('path', appContext.state.currentFolder.path);

		const uploadingFiles = appContext.state.uploadingFiles;

		uploadingFiles.push(file);

		appContext.setState({ uploadingFiles : uploadingFiles });

		axios.request({
				url     : URL,
				method  : 'POST',
				data    : formData,
				onUploadProgress : p => appContext.setState({
						[`uploadingFile${file.name}progress`] : (p.total - (p.total - p.loaded)) / p.total * 100
				})
			})
			.then(async response => {
				AppContentContext.setFiles(await APIHelpers.getFolderFiles(appContext.state.currentFolder.id));

				if (response.data !== "OK") APIHelpers.errorNotification("Error uploading");
			})
			.catch(() => APIHelpers.errorNotification("Error starting upload"));
	};

	static uploadFiles = async (files: Array<File>, app: App) => files.forEach(file => APIHelpers.uploadFile(file, app));

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

		if (AppNotificationContext) {
			const notifications = AppNotificationContext.notifications;

			notifications.push(NotificationHelpers.createErrorNotification(text, ErrorNotificationType.ERROR));

			AppNotificationContext.setNotifications(notifications);
		}
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
		const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
		return v.toString(16);
	});
}

export class NotificationHelpers {

	static createNotification = (text : string) : NotificationEntity => {
		return {
			id 		: EntityHelpers.uuidv4(),
			text	: text,
			type	: NotificationType.BASIC
		};
	};

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

	static createContentContext = (app : App) : ContentContextInterface => {
		return {
			files		: [],
			folders		: [],
			trashItems	: [],
			setFiles: (newFiles: FileEntity[] = []) => {
				AppContentContext.files = newFiles;

				app.forceUpdate();

				return AppContentContext.files;
			},
			setFolders: (newFolders: FolderEntity[] = []) => {
				AppContentContext.folders = newFolders;

				app.forceUpdate();

				return AppContentContext.folders;
			},
			setTrashItems: (newTrashItems: Entity[] = []) => {
				AppContentContext.trashItems = newTrashItems;

				app.forceUpdate();

				return AppContentContext.trashItems;
			}
		};
	};

	static createNotificationContext = (app : App) : NotificationsContextInterface => {
		return {
			notifications : [],
			setNotifications : (notifications : NotificationEntity[]) => {
				AppNotificationContext.notifications = notifications;

				app.forceUpdate();

				return AppNotificationContext.notifications;
			},
			deleteNotification : (id : string) => {
				const notifications = AppNotificationContext.notifications;

				notifications.splice(notifications.findIndex(n => n.id === id + 1), 1);

				AppNotificationContext.setNotifications(notifications);
			}
		};
	};

	static createProcessingContext = (app : App) : ProcessingContext => {
		return {
			entities : [],
			add : (entity : Entity) : Entity | null => {
				AppProcessingContext.entities.push(entity);

				app.forceUpdate();

				return AppProcessingContext.get(entity.id);
			},
            get	: (id : string) : Entity | null => AppProcessingContext.entities.filter(e => e.id === id)[0],
			delete : (id : string) => {
				const index = AppProcessingContext.entities.findIndex(e => e.id === id);

				AppProcessingContext.entities.splice(index, 1);
			}
		};
	};
}
