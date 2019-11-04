import { FolderEntity }	from './model/entity/FolderEntity';
import { Entity }		from './model/entity/Entity';
import NavNode			from './model/NavNode';

import App, { AppContentContext } from './App';

import axios from 'axios';

export const API_URL = 'http://localhost:8080';

export class APIHelpers {

	static getRootId = () : Promise<string> => (
		axios.get(`${API_URL}/folder/root/id`)
			.then(id => id.data)
			.catch(console.log)
	);

	static getRootData = () : Promise<any> => (
		axios.get(`${API_URL}/folder/root/data`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getRootMemory = () : Promise<object> => (
		axios.get(`${API_URL}/folder/root/memory`)
			.then(memory => memory.data)
			.catch(console.log)
	);

	static getFolderPredecessors = (id : string) : Promise<FolderEntity[]> => (
		axios.get(`${API_URL}/folder/${id}/predecessors`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getFileData = (id: string) : Promise<Entity> => (
		axios.get(`${API_URL}/file/${id}/data`)
			.then(response => response.data)
			.catch(console.log)
	);
 
	static getFolderData = (id: string) : Promise<FolderEntity> => (
		axios.get(`${API_URL}/folder/${id}/data`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getFolderFiles = (id: string) : Promise<[]> => (
		axios.get(`${API_URL}/folder/${id}/files`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getFolderFolders = (id: string) : Promise<[]> => (
		axios.get(`${API_URL}/folder/${id}/folders`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getTrashItems = (): Promise<[]> => (
		axios.get(`${API_URL}/trash/items`)
			.then(response => response.data)
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

	static createNewFolder = (folder: FolderEntity, path: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/create/`,
			{
				name		: folder,
				folderPath	: path
			})
			.then(response => response.data)
	);

	static pasteEntity = (target: Entity, action: string, newPath: string) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/${action}`,
			{
				oldPath: target.path,
				newPath: newPath
			})
			.then(response => response.data)
	);

	static moveToTrash = (target: Entity) : Promise<string> => (
		axios.post(`${API_URL}/trash/${target.type.toLowerCase()}/move-to-trash`,
			{
				id : target.id
			})
			.then(response => response.data)
	);

	static restoreFromTrash = (target: Entity) : Promise<string> => (
		axios.post(`${API_URL}/trash/restore-from-trash`,
			{
				id : target.id
			})
			.then(response => response.data)
	);

	static deleteFromTrash = (target: Entity) : Promise<string> => (
		axios.delete(`${API_URL}/trash/delete/${target.id}`)
			.then(response => response.data)
	);

	static emptyTrash = () : Promise<string> => (
		axios.delete(`${API_URL}/trash/empty-trash`)
			.then(response => response.data)
	);

	static renameEntity = (target: Entity, newName: string) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/rename`,
			{
				'id': target.id,
				'newName': newName
			})
			.then(response => response.data)
	);

	static deleteEntity = (target: Entity) : Promise<string> => (
		axios.delete(`${API_URL}/${target.type.toLowerCase()}/${target.id}/delete`)
			.then(response => response.data)
	);

	static deleteAllFromFolder = (path: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/delete-all`,
			{
				path: path
			})
			.then(response => response.data)
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
			.then(response => response.data === 'OK'
					? appContext.updateFolder(appContext.state.currentFolder.id)
					: console.log(response.data))
			.catch(console.log);
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
		const zip = await APIHelpers.zipFolder(target.path);

		if (zip !== "folder is empty" && await APIHelpers.folderHasContent(target.id)) {
			await APIHelpers.downloadFile(zip, target.name);
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
			async () => await app.updateFolder(entity.id)
				.then(() => NavigationNodesHelpers.removeNodeSuccessors(entity.id, app)));
	};

	static removeNodeSuccessors = async (id: string, app: App) => {
		const nodes = app.state.foldersNavigation;

		nodes.splice(nodes.findIndex(node => node.id === id) + 1, 9e9);

		app.setState({ foldersNavigation : nodes });
	};
}