import {FolderEntity} 	from './model/entity/FolderEntity';
import {Entity} 		from './model/entity/Entity';

import App from './App';

import axios from 'axios';

export const API_URL = 'http://localhost:8080';

export class APIHelpers {

	static getRootMemory = () : Promise<object> => (
		axios.get(`${API_URL}/folder/root/memory`)
			.then(memory => memory.data)
			.catch(console.log)
	);

	static folderHasContent = (id: number) : Promise<boolean> => (
		axios.get(`${API_URL}/folder/${id}/content_info`)
			.then(response => response.data.folderCount > 0 || response.data.filesCount > 0)
			.catch(_ => false)
	);
 
	static getFolderData = (id: number) : Promise<FolderEntity> => (
		axios.get(`${API_URL}/folder/${id}/data`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getFolderFiles = (id: number) : Promise<[]> => (
		axios.get(`${API_URL}/folder/${id}/files`)
			.then(response => response.data)
			.catch(console.log)
	);

	static getFolderFolders = (id: number) : Promise<[]> => (
		axios.get(`${API_URL}/folder/${id}/folders`)
			.then(response => response.data)
			.catch(console.log)
	);

	static folderZipRequest = (folderPath: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/zip`,
			{
				path: folderPath
			})
			.then(response => response.data)
	);

	static sendNewFolder = (folder: FolderEntity, path: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/create/`,
			{
				'name' 		: folder,
				'folderPath': path
			})
			.then(response => response.data)
	);

	static sendPasteRequest = (target: Entity, action: string, newPath: string) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/${action}`,
			{
				oldPath: target.path,
				newPath: newPath
			})
			.then(response => response.data)
	);

	static sendRenameRequest = (target: Entity, newName: string) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/rename`,
			{
				'path': target.path,
				'newName': newName
			})
			.then(response => response.data)
	);

	static sendDeleteRequest = (target: Entity) : Promise<string> => (
		axios.post(`${API_URL}/${target.type.toLowerCase()}/delete`,
			{
				'path' : target.path
			})
			.then(response => response.data)
	);

	static sendDeleteAll = (path: string) : Promise<string> => (
		axios.post(`${API_URL}/folder/delete-all`,
			{
				path: path
			})
			.then(response => response.data)
	);

	static uploadFile = async (file: File, appContext: App) => {
		const URL = `${API_URL}/file/upload/one`;

		const formData = new FormData();

		formData.append('file', file);
		formData.append('path', appContext.state.folderInfo.path);

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
					? appContext.updateFolderInfo(appContext.state.folderInfo.id)
					: console.log(response.data))
			.catch(console.log);
	};

	static downloadFile = async (path: string, name: string) => {
		const link = document.createElement("a");
		
		link.download = name;

		link.href = `${API_URL}/file/${path.replace(/[/]/g, '%2F')}/${name}/download`;

		document.body.appendChild(link);
		
		link.click();
	};
}