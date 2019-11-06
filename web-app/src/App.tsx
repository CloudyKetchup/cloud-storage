import {BrowserRouter as Router, Link, Route, Switch}	from 'react-router-dom';
import React, {Component, Context, createContext} 		from 'react';

import { APIHelpers as API, NavigationNodesHelpers }	from './helpers';
import { BufferElement }  				from './model/BufferElement';
import { ContentContextInterface } 		from './context/ContentContext';
import { ElementInfoContainer }         from './components/ElementInfoContainer/ElementInfoContainer';
import { Entity }         				from './model/entity/Entity';
import { EntityType }     				from './model/entity/EntityType';
import { FileEntity } 					from './model/entity/FileEntity';
import { FolderEntity }   				from './model/entity/FolderEntity';
import BufferElementIndicator           from './components/BufferElement/BufferElementIndicator';
import ContentContainer                 from './components/ContentContainer/ContentContainer';
import { ContentTreeView }				from './components/ContentTreeView/ContentTreeView';
import CreateFolderDialog               from './components/CreateFolderDialog/CreateFolderDialog';
import DragAndDrop                      from './components/DragAndDrop/DragAndDrop';
import FileUploadManager, { UploadFile} from './components/UploadManager/UploadManager';
import LeftPanel                        from './components/LeftPanel/LeftPanel';
import NavBar                           from './components/NavBar/NavBar'
import NavNode 							from './model/NavNode';
import PrevFolderButton                 from './components/PrevFolderButton/PrevFolderButton';
import RenameEntityDialog 				from './components/RenameEntityDialog/RenameEntityDialog';
import RightPanel                       from './components/RightPanel/RightPanel';
import TrashContainer					from './components/TrashContainer/TrashContainer';
import ImageViewOverlay 				from './components/ImageViewOverlay/ImageViewOverlay';

type IState = {
	bufferElement		: BufferElement | undefined,
	elementSelected		: Entity		| undefined,
	currentFolder		: FolderEntity,
	rootOpened			: boolean,
	rootMemory			: object,
	uploadingFiles		: File[],
	foldersNavigation 	: NavNode[],
	[fileUploadProgress : string] : any,
};

export let AppContentContext : ContentContextInterface; 

export let ContentContext : Context<ContentContextInterface>;

export default class App extends Component<{ data : FolderEntity }> {
	state : IState = {
		bufferElement: undefined,
		elementSelected: undefined,
		currentFolder: this.props.data,
		rootOpened: true,
		rootMemory: {},
		uploadingFiles: [],
		foldersNavigation: [],
	};

	componentWillMount() {
		AppContentContext = {
			files		: [],
			folders		: [],
			trashItems	: [],
			setFiles: (newFiles: FileEntity[] = []) => {
				AppContentContext.files = [...newFiles];

				this.forceUpdate();
				
				return AppContentContext.files },
			setFolders: (newFolders: FolderEntity[] = []) => {
				AppContentContext.folders = [...newFolders];

				this.forceUpdate();
				
				return AppContentContext.folders },
			setTrashItems: (newTrashItems: Entity[] = []) => {
				AppContentContext.trashItems = [...newTrashItems];

				this.forceUpdate();
				
				return AppContentContext.trashItems }
		};
		ContentContext = createContext(AppContentContext);
	}

	componentDidMount = async () => {
		this.setState({ 
			rootMemory : await API.getRootMemory(),
			rootOpened : true
		});
		API.getTrashItems().then(AppContentContext.setTrashItems);

		this.updateFolder();
	};

	updateFolder = async (folderId = this.state.currentFolder.id) => {
		API.getFolderData(folderId).then(data => {
			this.setState({
				elementSelected : undefined,
				currentFolder: data,
				rootOpened : data.root,
			});
		});

		API.getFolderFiles(folderId).then(AppContentContext.setFiles);

		API.getFolderFolders(folderId).then(AppContentContext.setFolders);

		const nodes : NavNode[] = [];

		API.getFolderPredecessors(folderId).then(predecessors => {
			predecessors.forEach(p => nodes.push(NavigationNodesHelpers.createNavNode(p, nodes[nodes.length -1], this)));
		})
		.then(() => this.setState({ foldersNavigation : [...nodes, this.state.currentFolder] }));
	};

	handleContextMenuAction = async (action: string, target: Entity) => {
		switch (action) {
			case 'download':
				if (target.type === EntityType.FILE) {
					await API.downloadFile(target.path, target.name);
				} else if (target.type === EntityType.FOLDER) {
					await API.downloadFolder(target as FolderEntity);
				}
				break;
			case 'move':
				this.setState({ bufferElement: {
					action: 'move',
					data: target
				}});
				break;
			case 'copy':
				this.setState({ bufferElement: {
					action: 'copy',
					data: target
				}});
				break;
			case 'delete':
				this.deleteEntity(target);
				break;
			case 'trash':
				const result = await API.moveToTrash(target);

				if (result === "OK") {
					this.updateFolder();

					API.getTrashItems().then(items => AppContentContext.setTrashItems(items));
				}
				break;
			default: break;
		}
	};

	createNewFolder = async (folder: FolderEntity) => {
		const result = await API.createNewFolder(folder, this.state.currentFolder.path);

		if (result === "OK") this.updateFolder()
	};

	pasteEntity = async (targetEntity = this.state.bufferElement) => {
		if (targetEntity !== undefined) {
			const result = await API.pasteEntity(targetEntity.data, targetEntity.action, this.state.currentFolder.path);

			if (result === "OK") this.updateFolder()
		}
	};

	renameEntity = async (target : Entity, newName: string) => {
		const result = await API.renameEntity(target, newName);

		if (result === "OK") this.updateFolder();
	};

	deleteEntity = async (target: Entity) => {
		const result = await API.deleteEntity(target);

		if (result === "OK") this.updateFolder()
	};

	moveToTrash = async (target: Entity) => {
		const result = await API.moveToTrash(target);

		if (result === "OK") {
			this.updateFolder().then(() => API.getTrashItems().then(AppContentContext.setTrashItems));
		}
	};

	deleteAllInFolder = async () => {
		const result = await API.deleteAllFromFolder(this.state.currentFolder.path);

		if (result === "OK") this.updateFolder()
	};

	render = () => (
		<Router>
			<div style={{ height: '100%' }}>
				<NavBar navNodes={this.state.foldersNavigation} />
				<LeftPanel
					memory={this.state.rootMemory}
					currentFolder={this.state.currentFolder}
					folders={AppContentContext.folders.length}
					files={AppContentContext.files.length}
				>
					<ContentContext.Provider value={AppContentContext}>
						<ContentTreeView app={this} />
					</ContentContext.Provider>
				</LeftPanel>
				<DragAndDrop
					className="drag-and-drop"
					style={{ height: "100%" }}
					handleDrop={(files: Array<File>) => API.uploadFiles(files, this)}>
					<Switch>
						<Route exact path="/:type/:id/rename" render={props =>
							<RenameEntityDialog
								onRename={(target: Entity, newName: string) => this.renameEntity(target, newName)}
								{...props} />}
						/>
						<Route exact path="/folder/create" render={() =>
							<CreateFolderDialog
								parent={this}
								sendFolder={(folder: FolderEntity) => this.createNewFolder(folder)}/>}/>
						<Route path="/:type/:id/info" render={props => <ElementInfoContainer key={props.match.params.id} {...props}/>}/>
						<Route exact path="/trash" render={() => <TrashContainer app={this}/>}/>
						<Route path="/file/image/:id/view" render={props => <ImageViewOverlay id={props.match.params.id}/>}/>
					</Switch>
					<ContentContext.Provider value={AppContentContext}>
						<ContentContainer
							folderId={this.state.currentFolder.id}
							parent={this}>
							<PrevFolderButton
								whenClicked={async () => {
									if (this.state.currentFolder.parentId) {
										await NavigationNodesHelpers.removeNodeSuccessors(this.state.currentFolder.parentId, this);

										await this.updateFolder(this.state.currentFolder.parentId);
									}
								}}
								rootOpened={this.state.rootOpened}
							/>
							<Link to="/folder/create">
								<button className='create-folder'><i className='fas fa-folder-plus'/></button>
							</Link>
							<button
								className='upload-file-button'
								onClick={() => {
									const uploadInput = document.getElementById('select-upload-files');

									if (uploadInput) uploadInput.click();
								}}
							><i className='fas fa-file-upload'/></button>
							{this.state.uploadingFiles.length > 0
								&&
								<FileUploadManager onClose={() => this.setState({ uploadingFiles: [] })}>
									{this.state.uploadingFiles.map(file => <UploadFile key={file.name} data={file} parent={this} />)}
								</FileUploadManager>}
							{this.state.bufferElement !== undefined && <BufferElementIndicator element={this.state.bufferElement} />}
						</ContentContainer>
					</ContentContext.Provider>
				</DragAndDrop>
				<RightPanel closePanel={() => {
					const rightPanel = document.getElementById("right-panel");

					if (rightPanel) rightPanel.style.right = '-300px';
				}} />
				<input
					id="select-upload-files"
					type="file"
					onChange={async () => {
						const files = (document.getElementById("select-upload-files") as HTMLInputElement).files;

						if (files) await API.uploadFiles(Array.from(files), this);
					}}
					style={{ display: 'none' }}
					multiple />
			</div>
		</Router>
	);
}
