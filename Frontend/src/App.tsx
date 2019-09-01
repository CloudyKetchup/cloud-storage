import React, {Component} from 'react';

import NavBar                           from './components/main/navbar/NavBar'
import LeftPanel                        from './components/main/panel/leftpanel/LeftPanel';
import ContentContainer                 from './components/content/container/ContentContainer';
import PrevFolderButton                 from './components/content/container/control/PrevFolderButton';
import BufferElementIndicator           from './components/bufferelement/BufferElementIndicator';
import {ElementInfoContainer}           from './components/content/elements/info/ElementInfoContainer';
import DragAndDrop                      from './components/content/dragdrop/DragAndDrop';
import FileUploadManager, {UploadFile}  from './components/content/upload/UploadManager';
import RightPanel                       from './components/main/panel/rightpanel/RightPanel';
import CreateFolderDialog               from './components/content/container/control/CreateFolderDialog';
import RenameElementDialog              from './components/content/elements/rename/RenameElementDialog';
import FolderZippingNotification        from './components/main/panel/rightpanel/notification/FolderZippingNotification/FolderZippingNotification';
import ErrorNotification        		from './components/main/panel/rightpanel/notification/ErrorNotification/ErrorNotification';
import {NotificationType}               from './components/main/panel/rightpanel/notification/Notification';
import {BrowserRouter as Router, Link, Route, Switch} from 'react-router-dom';

import {FolderEntity}   from './model/entity/FolderEntity';
import {FileEntity}     from './model/entity/FileEntity';
import {Entity}         from './model/entity/Entity';
import {BufferElement}  from './model/BufferElement';
import {Notification}   from './model/notification/Notification';
import {EntityType}     from './model/entity/EntityType';
import NavNode 			from './model/NavNode';

import {APIHelpers as API} from './helpers';

type IState = {
	folders			: FolderEntity[],
	files			: FileEntity[],
	bufferElement		: BufferElement | undefined,
	elementSelected		: Entity	| undefined,
	folderInfo		: FolderEntity,
	rootOpened		: boolean,
	rootMemory		: object,
	uploadingFiles		: File[],
	notifications		: Notification[],
	notificationKey 	: number,
	foldersNavigation 	: NavNode[],
	[fileUploadProgress : string] : any
};

export default class App extends Component<{}, IState> {
	state: IState = {
		folders             : [],
		files               : [],
		bufferElement       : undefined,
		elementSelected     : undefined,
		folderInfo          : {
			id          : 1,
			name        : 'cloud',
			parentId    : null,
			timeCreated : undefined,
			root        : true,
			path        : '',
			location    : null,
			type        : EntityType.FOLDER
		},
		rootOpened          : true,
		rootMemory          : {},
		uploadingFiles      : [],
		notifications       : [],
		notificationKey		: 0,
		foldersNavigation 	: []
	};

	componentDidMount() {
		API.getRootMemory().then(memory => {
			this.setState({
				rootMemory : memory,
				rootOpened : true
			});
		});

		this.addNavNode(this.state.folderInfo);

		this.updateFolderInfo(1);
	}

	updateFolderInfo = (folderId: number) => {
		this.setState({ elementSelected : undefined });

		API.getFolderData(folderId).then(data => {
			this.setState({
				folderInfo : data,
				rootOpened : data.root,			
			})
		});

		API.getFolderFiles(folderId).then(files => this.setState({ files : files }));

		API.getFolderFolders(folderId).then(folders => this.setState({ folders : folders }));	
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
			case 'cut':
				this.setState({bufferElement: {
					action: 'cut',
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
			default: break;
		}
	};

	addNavNode = async (entity: FolderEntity) => {
		const nodes = this.state.foldersNavigation;

		const prevNode = nodes.slice(-1).pop();

		const node = new NavNode(
			entity.id,
			entity.root ? '/' : entity.name,
			prevNode,
			() => {
				this.updateFolderInfo(entity.id);

				this.removeNodeSuccessors(entity.id);
			});

		if (prevNode !== undefined) prevNode.next = node;

		nodes.push(node);

		this.setState({ foldersNavigation : nodes });
	};

	removeNodeSuccessors = async (id: number) => {
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
			key		: this.state.notificationKey,
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
			.catch(_ => this.errorNotificationProcessing(key));
	};

	createNewFolder = (folder: FolderEntity) => {
		API.sendNewFolder(folder, this.state.folderInfo.path)
			.then(response => response === 'OK' 
				? this.updateFolderInfo(this.state.folderInfo.id) 
				: console.log(response))
			.catch(console.log);
	};

	pasteEntity = (targetEntity = this.state.bufferElement) => {
		if (targetEntity !== undefined) {
			API.sendPasteRequest(targetEntity.data, targetEntity.action, this.state.folderInfo.path)
				.then(response => response === 'OK'
					? this.updateFolderInfo(this.state.folderInfo.id)
					: console.log(response))
				.catch(console.log);
		}
	};

	renameEntity = (newName: string) => {
		if (this.state.elementSelected !== undefined) {
			API.sendRenameRequest(this.state.elementSelected, newName)
				.then(response => {
					if (response === 'OK') {
						this.updateFolderInfo(this.state.folderInfo.id);
					}
				})
				.catch(console.log);
		}
	};

	sendDeleteRequest = (target: Entity) => {
		API.sendDeleteRequest(target)
			.then(response => response === 'OK'
				? this.updateFolderInfo(this.state.folderInfo.id)
				: console.log(response))
			.catch(console.log);
	};

	sendDeleteAll() {
		API.sendDeleteAll(this.state.folderInfo.path)
			.then(response => response === 'OK'
				? this.updateFolderInfo(this.state.folderInfo.id)
				: console.log(response))
			.catch(console.log);
	}

	uploadFiles = (files: Array<File>) => {
		for(let file of files) {
			API.uploadFile(file, this);
		}
	};

	createFolderDialog() {
		return 	<CreateFolderDialog
			parent={this}
			sendFolder={(folder: FolderEntity) => this.createNewFolder(folder)}
		/>;
	}

	renameDialog() {
		return this.state.elementSelected !== undefined
			? <RenameElementDialog
				element={this.state.elementSelected}
				onRename={(newName: string) => this.renameEntity(newName)}
			/>
			: null;
	}

	elementInfoContainer() {
		return this.state.elementSelected !== undefined
			? <ElementInfoContainer parent={this} data={this.state.elementSelected}/>
			: null;
	}

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
			<div style={{height : '100%'}}>
				<NavBar notifications={this.state.notifications} navNodes={this.state.foldersNavigation}/>
				<LeftPanel
					memory={this.state.rootMemory}
					folderInfo={this.state.folderInfo}
					folders={this.state.folders.length}
					files={this.state.files.length}
				/>
				<DragAndDrop className="drag-and-drop" handleDrop={this.uploadFiles}>                    
					<ContentContainer
						parent={this}
						files={this.state.files}
						folders={this.state.folders}
					>
						<Switch>
							<Route path="/rename" component={() => this.renameDialog()}/>
							<Route path="/create-folder" component={() => this.createFolderDialog()}/>
							<Route path="/element-info" component={() => this.elementInfoContainer()}/>
						</Switch>
						<PrevFolderButton
							whenClicked={() => {
								if (this.state.folderInfo.parentId !== null) {
									this.removeNodeSuccessors(this.state.folderInfo.parentId);

									this.updateFolderInfo(this.state.folderInfo.parentId);
								}
							}}
							rootOpened={this.state.rootOpened}
						/>
						<Link to="/create-folder">
							<button
								className='create-folder'
							>
								<i className='fas fa-folder-plus'/>
							</button>
						</Link>
						<button
							className='upload-file-button'
							onClick={() => {
								const uploadInput = document.getElementById('select-upload-files');

								if (uploadInput !== null) uploadInput.click();
							}}
						>
							<i className='fas fa-file-upload'/>
						</button>
	{this.state.uploadingFiles.length > 0
		&&
			<FileUploadManager onClose={() => this.setState({ uploadingFiles : [] })}>
				{this.state.uploadingFiles.map(file => <UploadFile key={file.name} data={file} parent={this}/>)}
			</FileUploadManager>}    
		{this.state.bufferElement !== undefined
				&&
					<BufferElementIndicator element={this.state.bufferElement}/>}
		</ContentContainer>
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
					const filesDom = document.getElementById("select-upload-files") as HTMLInputElement;

					const files = filesDom.files;

					if (files !== null) files.length > 1 ? this.uploadFiles(Array.from(files)) : API.uploadFile(files[0], this);
				}}
				style={{ display : 'none' }}
				multiple/>
		</div>
		</Router>
	);
}
