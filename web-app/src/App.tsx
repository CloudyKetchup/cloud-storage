import { BrowserRouter as Router, Link, Route, Switch }	from 'react-router-dom';
import React, { Component, Context, createContext } 	from 'react';

import { APIHelpers as API, NavigationNodesHelpers, ContextHelpers, APIHelpers, EntityHelpers } from './helpers';
import { ContentContextInterface }		from './context/ContentContext';
import { ElementInfoContainer }			from './components/ElementInfoContainer/ElementInfoContainer';
import BufferElementIndicator			from './components/BufferElement/BufferElementIndicator';
import ContentContainer					from './components/ContentContainer/ContentContainer';
import { ContentTreeView }				from './components/ContentTreeView/ContentTreeView';
import CreateFolderDialog				from './components/CreateFolderDialog/CreateFolderDialog';
import DragAndDrop						from './components/DragAndDrop/DragAndDrop';
import LeftPanel						from './components/LeftPanel/LeftPanel';
import NavBar							from './components/NavBar/NavBar'
import PrevFolderButton					from './components/PrevFolderButton/PrevFolderButton';
import RenameEntityDialog 				from './components/RenameEntityDialog/RenameEntityDialog';
import ImageViewOverlay 				from './components/ImageViewOverlay/ImageViewOverlay';
import { BufferElement }  				from './model/BufferElement';
import { Entity }						from './model/entity/Entity';
import { EntityType }					from './model/entity/EntityType';
import { FolderEntity }					from './model/entity/FolderEntity';
import NavNode 							from './model/NavNode';
import NotificationComponentFactory 	from './factory/NotificationComponentFactory';
import { NotificationsContextInterface }from './context/NotificationContext';
import { ProcessingContext } 			from "./context/ProcessingContext";
import TrashViewContainer 				from './components/TrashViewContainer/TrashViewContainer';
import UploadingPane                    from './components/UploadingPane/UploadingPaneComponent';

type IState = {
	bufferElement		: BufferElement | undefined,
	elementSelected		: Entity		| undefined,
	rootMemory			: object,
	foldersNavigation	: NavNode[],
};

export let AppContentContext : ContentContextInterface; 

export let AppNotificationContext : NotificationsContextInterface;

export let AppProcessingContext : ProcessingContext;

export let ContentContext : Context<ContentContextInterface>;

export default class App extends Component<{ data : FolderEntity }> {
	state : IState = {
		bufferElement		: undefined,
		elementSelected		: undefined,
		rootMemory			: {},
		foldersNavigation	: []
	};

	UNSAFE_componentWillMount = () => {
		AppContentContext = ContextHelpers.createContentContext(this.props.data, this);

		AppNotificationContext = ContextHelpers.createNotificationContext(this);

		AppProcessingContext = ContextHelpers.createProcessingContext(this);

		ContentContext = createContext(AppContentContext);
	};

	componentDidMount = async () => {
		this.setState({
			rootMemory : await API.getRootMemory() || "",
			currentFolder : AppContentContext.currentFolder
		});

		API.getTrashItems().then(AppContentContext.setTrashItems);

		this.updateFolder();
	};

	updateFolder = async (folderId = AppContentContext.currentFolder.id) => {
		const data = await API.getFolderData(folderId);

		this.setState({
			elementSelected: undefined,
			rootOpened: data.root,
		});

		AppContentContext.setCurrentFolder(data);

		API.getFolderFiles(folderId).then(AppContentContext.setFiles);

		API.getFolderFolders(folderId).then(AppContentContext.setFolders);

		const nodes : NavNode[] = [];

		API.getFolderPredecessors(folderId).then(predecessors => {
			predecessors.forEach(p => nodes.push(NavigationNodesHelpers.createNavNode(p, nodes[nodes.length -1], this)));
		})
		.then(() => this.setState({ foldersNavigation : [...nodes, AppContentContext.currentFolder] }));
	};

	handleContextMenuAction = async (action: string, target: Entity) => {
		switch (action) {
			case 'download':
				if (target.type === EntityType.FILE)
					await API.downloadFile(target.path, target.name);
				else if (target.type === EntityType.FOLDER)
					this.downloadFolder(target as FolderEntity);
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
				if (await API.moveToTrash(target) === "OK") {
					this.updateFolder();

					API.getTrashItems().then(AppContentContext.setTrashItems);
				}
				break;
			default: break;
		}
	};

	downloadFolder = async (target : FolderEntity) => {
		AppProcessingContext.add(target);

		await API.downloadFolder(target as FolderEntity);

		AppProcessingContext.delete(target.id);
	};

	createNewFolder = async (name: string) => {
		const result = await API.createNewFolder(name, AppContentContext.currentFolder.path);

		if (result === "OK")
			this.updateFolder();
		else
			APIHelpers.errorNotification(`Error creating folder ${name}`);
	};

	pasteEntity = async (target = this.state.bufferElement) => {
		if (target) {
			const result = await API.pasteEntity(target.data, target.action, AppContentContext.currentFolder.path);

			if (result === "OK")
				this.updateFolder();
			else
				APIHelpers.errorNotification(`Error pasting ${target.data.name}`);
		}
	};

	renameEntity = async (target : Entity, newName: string) => {
		const result = await API.renameEntity(target, newName);

		if (result === "OK")
			this.updateFolder();
		else
			APIHelpers.errorNotification(`Error renaming ${target.name} to ${newName}`);
	};

	deleteEntity = async (target: Entity) => {
		const result = await API.deleteEntity(target);

		if (result === "OK")
			this.updateFolder();
		else
			APIHelpers.errorNotification(`Error deleting ${target.name}`);
	};

	moveToTrash = async (target: Entity) => {
		const result = await API.moveToTrash(target);

		if (result === "OK")
			this.updateFolder().then(() => API.getTrashItems().then(AppContentContext.setTrashItems));
		else
			API.errorNotification(`Error moving to trash ${target.name}`);
	};

	deleteFolderContent = async () => {
		const result = await API.deleteFolderContent(AppContentContext.currentFolder.id);

		if (result === "OK")
			this.updateFolder();
		else
			APIHelpers.errorNotification(`Error deleting folder ${AppContentContext.currentFolder.name} content`);
	};

	render = () => (
		<Router>
			<div style={{ height: '100%' }}>
				<NavBar navNodes={this.state.foldersNavigation} />
				<LeftPanel
					memory={this.state.rootMemory}
					currentFolder={AppContentContext.currentFolder}
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
					handleDrop={(files : Array<File>) => API.uploadFiles(AppContentContext.currentFolder, files)}>
					<Switch>
						<Route exact path="/:type/:id/rename" render={props =>
							<RenameEntityDialog
								onRename={this.renameEntity}
								{...props} />}
							/>
							<Route exact path="/folder/create" render={() => <CreateFolderDialog parent={this} sendFolder={this.createNewFolder}/>}/>
							<Route exact path="/info" render={props => <ElementInfoContainer key={props.match.params.id} {...props}/>}/>
							<Route path="/trash" render={() => <TrashViewContainer/>}/>
							<Route path="/image/view" render={() => <ImageViewOverlay key={EntityHelpers.uuidv4()}/>}/>
						</Switch>
						<ContentContext.Provider value={AppContentContext}>
							<ContentContainer
								folderId={AppContentContext.currentFolder.id}
								parent={this}>
								{
									AppContentContext.currentFolder.parentId
									&&
									<PrevFolderButton
										whenClicked={async () => {
											if (AppContentContext.currentFolder.parentId) {
												await NavigationNodesHelpers.removeNodeSuccessors(AppContentContext.currentFolder.parentId, this);

												await this.updateFolder(AppContentContext.currentFolder.parentId);
											}
										}}
									/>
								}
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
								{this.state.bufferElement && <BufferElementIndicator element={this.state.bufferElement} />}
								{
									AppNotificationContext.notifications.length > 0
									&&
									<div className="notifications-pad">
										{AppNotificationContext.notifications.map(NotificationComponentFactory.build)}
									</div>
								}
							</ContentContainer>
						</ContentContext.Provider>
					</DragAndDrop>
				<UploadingPane/>
				<input
					id="select-upload-files"
						type="file"
						onChange={() => {
							const files = (document.getElementById("select-upload-files") as HTMLInputElement).files;

							files && API.uploadFiles(AppContentContext.currentFolder, Array.from(files));
						}}
						style={{ display: 'none' }}
						multiple />
					</div>
				</Router>
	);
}
