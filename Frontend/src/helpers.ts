
import axios from 'axios';

const API_URL = 'http://localhost:8080';

export class FolderDataHelper {

	private static _instance : FolderDataHelper;

    private constructor() {}

    public static getInstance() {
       	return this._instance || (this._instance = new this());
    };

	folderHasContent(id: number) : Promise<boolean> {
		return axios.get(`${API_URL}/folder/${id}/content_info`)
			.then(response => response.data.folderCount > 0 || response.data.filesCount > 0)
			.catch(_ => false);
	};
}