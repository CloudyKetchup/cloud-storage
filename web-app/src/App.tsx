import {BrowserRouter as Router, Link, Route, Switch} from 'react-router-dom';
import React, {Component, Context, createContext} from 'react';

import { APIHelpers as API } 			from './helpers';
import { BufferElement }  				from './model/BufferElement';
import { ContentContextInterface } 		from './context/ContentContext';
import { ElementInfoContainer }         from './components/ElementInfoContainer/ElementInfoContainer';
import { Entity }         				from './model/entity/Entity';
import { EntityType }     				from './model/entity/EntityType';
import { FileEntity } 					from './model/entity/FileEntity';
import { FolderEntity }   				from './model/entity/FolderEntity';
import { Notification }					from './model/notification/Notification';
import { NotificationType }             from './components/Notification/Notification';
import BufferElementIndicator           from './components/BufferElement/BufferElementIndicator';
import ContentContainer                 from './components/ContentContainer/ContentContainer';
import { ContentTreeView }				from './components/ContentTreeView/ContentTreeView';
import CreateFolderDialog               from './components/CreateFolderDialog/CreateFolderDialog';
import DragAndDrop                      from './components/DragAndDrop/DragAndDrop';
import ErrorNotification				from './components/ErrorNotification/ErrorNotification';
import FileUploadManager, { UploadFile} from './components/UploadManager/UploadManager';
import FolderZippingNotification 		from './components/FolderZippingNotification/FolderZippingNotification';
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
	elementSelected		: Entity	| undefined,
	currentFolder		: FolderEntity,
	rootOpened			: boolean,
	rootMemory			: object,
	uploadingFiles		: File[],
	notifications		: Notification[],
	notificationKey 	: number,
	foldersNavigation 	: NavNode[],
	[fileUploadProgress : string] : any,
};

export let AppContentContext : ContentContextInterface; 

export let ContentContext : Context<ContentContextInterface>;

export default class App extends Component {
	state : IState = {
		bufferElement: undefined,
		elementSelected: undefined,
		currentFolder: {
			id: "",
			name: 'Cloud',
			parentId: null,
			timeCreated: undefined,
			root: true,
			path: '',
			location: null,
			size: "0",
			type: EntityType.FOLDER
		},
		rootOpened: true,
		rootMemory: {},
		uploadingFiles: [],
		notifications: [],
		notificationKey: 0,
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
			currentFolder: {
				id: await API.getRootId(),
				name: '',
				parentId: null,
				timeCreated: undefined,
				root: true,
				path: '',
				location: null,
				size: "0",
				type: EntityType.FOLDER
			},
			rootMemory : await API.getRootMemory(),
			rootOpened : true
		});
		API.getTrashItems().then(AppContentContext.setTrashItems);

		this.updateFolderInfo();
	}

	updateFolderInfo = (folderId = this.state.currentFolder.id) => {
		this.setState({ elementSelected : undefined });

		API.getFolderData(folderId).then(data => {
			this.setState({
				currentFolder: data,
				rootOpened : data.root,
			});
		});

		API.getFolderFiles(folderId).then(AppContentContext.setFiles);

		API.getFolderFolders(folderId).then(AppContentContext.setFolders)

		const nodes : NavNode[] = [];

		API.getFolderPredecessors(folderId).then(predecessors => {
			predecessors.forEach(p => nodes.push(this.createNavNode(p, nodes[nodes.length -1])));
		})
		.then(() => this.setState({ foldersNavigation : [...nodes, this.state.currentFolder] }));
	};

	handleContextMenuAction = async (action: string, target: Entity) => {
		switch (action) {
			case 'download':
				if (target.type === EntityType.FILE) {
					API.downloadFile(target.path, target.name);
				} else {
					API.folderHasContent(target.id)
						.then(result => {
							result ? this.sendZipRequest(target as FolderEntity) : this.folderEmptyNotification(target as FolderEntity)
						});
				}
				break;
			case 'move':
				this.setState({bufferElement: {
					action: 'move',
					data: target
				}});
				break;
			case 'copy':
				this.setState({bufferElement: {
					action: 'copy',
					data: target
				}});
				break;
			case 'delete':
				this.sendDeleteRequest(target);
				break;
			case 'trash':
				const result = await API.moveToTrash(target);

				if (result === "OK") {
					this.updateFolderInfo();

					API.getTrashItems().then(items => AppContentContext.setTrashItems(items));
				}
				break;
			default: break;
		}
	};

	createNavNode = (entity : FolderEntity, prevNode : NavNode | undefined) => {
		return new NavNode(
			entity.id,
			entity.name,
			prevNode,
			() => {
				this.updateFolderInfo(entity.id);

				this.removeNodeSuccessors(entity.id);
			});
	};

	removeNodeSuccessors = async (id: string) => {
		const nodes = this.state.foldersNavigation;

		nodes.splice(nodes.findIndex(node => node.id === id) + 1, 9e9);

		this.setState({ foldersNavigation : nodes });
	};

	addNotification = async (data: Notification) => {
		const notifications = this.state.notifications;

		notifications.push(data);

		let updatedKey = this.state.notificationKey;

		this.setState({
			notifications 	: notifications,
			notificationKey : ++updatedKey
		});
	};

	folderEmptyNotification = (target: FolderEntity) => {
		this.addNotification({
			key			: this.state.notificationKey,
			type		: NotificationType.ERROR,
			message 	: `Folder "${target.name}" is empty`,
			targetType 	: target.type,
			name 		: target.name,
			processing 	: false,
			error 		: true
		});
	};

	successNotificationProcessing = (key: number) => {
		const notifications = this.state.notifications;

		notifications.filter(notification => notification.key === key)[0].processing = false;

		this.setState({ notifications : notifications });
	};

	errorNotificationProcessing = (key: number) => {
		const notifications = this.state.notifications;

		notifications.filter(notification => notification.key === key)[0].error = true;

		this.setState({ notifications : notifications });
	};

	removeNotification = (key: number) => {
		const notifications = this.state.notifications;

		notifications.splice(notifications.findIndex(n => n.key === key), 1);

		this.setState({ notifications : notifications });
	};

	sendZipRequest = (target: FolderEntity) => {
		const key = this.state.notificationKey;

		this.addNotification({
			key         	: key,
			type 		: NotificationType.PROCESSING,
			message 	: `Processing "${target.name}"`,
			targetType 	: target.type,
			name 		: target.name,
			processing 	: true,
			error 		: false
		});

		API.folderZipRequest(target.path)
			.then(response => {
				switch (response) {
					case 'folder is empty':
						break;
					case 'INTERNAL_SERVER_ERROR':
						this.errorNotificationProcessing(key);
						break;
					default:
						this.successNotificationProcessing(key);

						API.downloadFile(response, `${target.name}.zip`);	
						break;
				}
			})
			.catch(() => this.errorNotificationProcessing(key));
	};

	createNewFolder = (folder: FolderEntity) => {
		API.sendNewFolder(folder, this.state.currentFolder.path)
			.then(response => response === 'OK' 
				? this.updateFolderInfo() 
				: console.log(response))
			.catch(console.log);
	};

	pasteEntity = (targetEntity = this.state.bufferElement) => {
		if (targetEntity !== undefined) {
			API.sendPasteRequest(targetEntity.data, targetEntity.action, this.state.currentFolder.path)
				.then(response => response === 'OK'
					? this.updateFolderInfo()
					: console.log(response))
				.catch(console.log);
		}
	};

	renameEntity = (target : Entity, newName: string) => {
		API.sendRenameRequest(target, newName)
			.then(response => {
				if (response === 'OK') this.updateFolderInfo();
			})
			.catch(console.log);
	};

	sendDeleteRequest = (target: Entity) => {
		API.sendDeleteRequest(target)
			.then(response => response === 'OK'
				? this.updateFolderInfo()
				: console.log(response))
			.catch(console.log);
	};

	sendDeleteAll() {
		API.sendDeleteAll(this.state.currentFolder.path)
			.then(response => response === 'OK'
				? this.updateFolderInfo()
				: console.log(response))
			.catch(console.log);
	}

	uploadFiles = (files: Array<File>) => {
		for(let file of files) {
			API.uploadFile(file, this);
		}
	};

	notificationDiv = (notification: Notification) => {
		if (notification.type === NotificationType.ERROR) {
			return <ErrorNotification 
				key={notification.key}
				id={notification.key}
				appContext={this}
				message={notification.message}
			/>
		} else {
			return <FolderZippingNotification
				appContext={this}
				key={notification.key}
				id={notification.key}
				folderName={notification.name}
				processing={notification.processing}
				error={notification.error}
			/>
		}
	};

	render = () => (
		<Router>
			<div style={{ height : '100%' }}>
				<NavBar notifications={this.state.notifications} navNodes={this.state.foldersNavigation}/>
				<LeftPanel
					memory={this.state.rootMemory}
					currentFolder={this.state.currentFolder}
					folders={AppContentContext.folders.length}
					files={AppContentContext.files.length}
				>
					<ContentContext.Provider value={AppContentContext}>
						<ContentTreeView app={this}/>
					</ContentContext.Provider>
				</LeftPanel>
				<DragAndDrop className="drag-and-drop" style={{ height : "100%" }} handleDrop={this.uploadFiles}>
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
						<Route path="/:type/:id/info" render={props => <ElementInfoContainer key={`${props.match.params.id}`} {...props}/>}/>
						<Route exact path="/trash" render={() => <TrashContainer parent={this}/>}/>
						<Route path="/file/image/:id/view" render={props => <ImageViewOverlay id={props.match.params.id}/>}/>
					</Switch>
					<ContentContext.Provider value={AppContentContext}>
						<ContentContainer
							folderId={this.state.currentFolder.id}
							parent={this}>
							<PrevFolderButton
								whenClicked={() => {
									if (this.state.currentFolder.parentId !== null) {
										this.removeNodeSuccessors(this.state.currentFolder.parentId);

										this.updateFolderInfo(this.state.currentFolder.parentId);
									}
								}}
								rootOpened={this.state.rootOpened}
							/>
							<Link to="/folder/create">
								<button className='create-folder'><i className='fas fa-folder-plus' /></button>
							</Link>
							<button
								className='upload-file-button'
								onClick={() => {
									const uploadInput = document.getElementById('select-upload-files');

									if (uploadInput !== null) uploadInput.click();
								}}
							><i className='fas fa-file-upload' /></button>
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

						if (rightPanel !== null) rightPanel.style.right = '-300px';
					}}>
						{this.state.notifications.map(this.notificationDiv)}
					</RightPanel>
					<input
						id="select-upload-files"
						type="file"
						onChange={() => {
							const files = (document.getElementById("select-upload-files") as HTMLInputElement).files;

							if (files !== null) files.length > 1 ? this.uploadFiles(Array.from(files)) : API.uploadFile(files[0], this);
						}}
						style={{ display : 'none' }}
						multiple/>
			</div>
		</Router>
	);
}
