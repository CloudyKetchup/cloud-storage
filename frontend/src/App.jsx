import React, { Component } from 'react';

import Nav 					from './components/Nav'
import SideBar 				from './components/SideBar';
import ContentElements 		from './components/ContentElements';
import Folder 				from './components/Folder';
import File 				from './components/File';
import PrevFolderButton 	from './components/PrevFolderButton';
import RenameElementDialog  from './components/RenameElementDialog'
import UploadFileDialog 	from './components/UploadFileDialog';
import CreateFolderDialog 	from './components/CreateFolderDialog';
import BufferElements 		from './components/BufferElements';

const axios = require('axios');

const API_URL = 'http://localhost:8080'

export default class App extends Component {
	constructor() {
		super();
		this.state = {
			elements 	: [],
			folders 	: [],
			files 		: [],
			bufferFile  : {},
			elementSelected : {},
			folderInfo 	: this.folderInfo,
			prevState 	: this.prevState,
			rootOpened 	: true,
			rootMemory	: {},
			renameElementDialog : false,
			createFolderDialog 	: false,
			uploadFileDialog 	: false
		};
		window.onkeyup = e => {
			var key = e.keyCode ? e.keyCode : e.which;

			if (key === 27)
				this.setState({
					elementSelected : undefined,
					createFolderDialog : false,
					uploadFileDialog : false
				})
		}
	}

	componentDidMount() {
		this.getRootContent();
		this.getRootMemory();		
	}

	getRootContent() {
		axios.get(`${API_URL}/folder/root/content`)
			.then(root =>
				this.setState({
					folders 	: root.data.folders,
					files 		: root.data.files,
					elements 	: this.sortElements(root.data.folders.concat(root.data.files)),
					folderInfo 	: root.data.rootFolder
				})
			)
			.catch(error => console.log(error));
	}

	getRootMemory() {
		axios.get(`${API_URL}/folder/root/memory`)
			.then(memory => 
				this.setState({
					rootMemory : memory.data
				})
			)
			.catch(error => console.log(error));
	}

	updateElementsData = (folderId = this.state.folderInfo.id) => {
		this.setState({ elementSelected : undefined });
		axios.get(`${API_URL}/folder/${folderId}/content`)
			.then(content => 
				this.setState(prevState => {
					return {
						folders 	: content.data.folders,
						files 		: content.data.files,
						elements 	: content.data.folders.concat(content.data.files),
						folderInfo 	: content.data.folderInfo,
						prevState	: prevState,
						rootOpened 	: false
					}
				})
			)
			.catch(error => console.log(error));
	}

	createElement(element) {
		return element.type === 'FILE' 
			? <File 
				key={element.id}
				name={element.name}
				/>
			: <Folder
				key={element.id}
				name={element.name}
				id={element.id}
				parent={this}
				handleAction={action => this.handleContextMenuAction(action, element)}
				whenClicked={() => 
						this.state.elementSelected !== undefined && this.state.elementSelected.id === element.id
							? this.updateElementsData(element.id)
							: this.setState({ elementSelected : element})
				}/>
	}

	handleContextMenuAction(action, element) {
		switch(action) {
			case 'cut':
				break;
			case 'copy':

				break;
			case 'rename':
				this.renameElementDialog(element);
				break;
			case 'delete':

				break;
			case 'info':

				break;
			default: break;
		}
	}

	renameElementDialog(element) {
		this.setState({ 
			'renameElementDialog' : true,
			'createFolderDialog'  : false,
			'uploadFileDialog' 	  : false 
		})
	}

	sendContextMenuAction(URL) {
		axios.post(URL)
			.then(response => console.log(response.data))
			.catch(error => console.log(error));
	}

	uploadFileDialog() {
		this.setState(prevState => ({
			'uploadFileDialog' 		: !prevState.uploadFileDialog,
			'createFolderDialog'	: false,
			'renameElementDialog' 	: false
		}));
	}

	createFolderDialog() {
		this.setState(prevState => ({
			'createFolderDialog'	: !prevState.createFolderDialog,
			'uploadFileDialog'		: false,
			'renameElementDialog' 	: false
		}));
	}

	sendNewFolder(folder) {
		axios.post(`${API_URL}/folder/create/`,
			{
				'name' 		: folder,
				'folderPath': this.state.folderInfo.path
			})
			.then(response => {
				if (response.data === 'OK') {
					
					this.updateElementsData();

					this.setState({ createFolderDialog : false });
				}
			})
			.catch(error => console.log(error));
	}

	sendRenameRequest(newName) {
		const renameTarget 	= this.state.elementSelected;
		const targetType   	= renameTarget.type.toLowerCase();

		axios.post(`${API_URL}/${targetType}/rename`,
			{
				[`${targetType}Path`] : renameTarget.path,
				'newName' : newName
			})
			.then(response => {
				if(response.data === 'OK') {
					this.setState({ renameElementDialog : false });
					this.updateElementsData();
				}
			})
			.catch(error => console.log(error));
    }

	sortElements(elements) {
		for(let i = 0; i < elements.length; i++) {
			for(let j = 0; j < elements.length; j++) {
				if (elements[i].id < elements[j].id) {
					const temp  = elements[i];
					elements[i] = elements[j];
					elements[j] = temp;
				}
			}
		}
		return elements;
	}

	render() {
		return (
			<main style={{height : '100%'}}>
				<Nav />
				<SideBar 
					memory 		= {this.state.rootMemory} 
					folderInfo 	= {this.state.folderInfo}
					folders 	= {this.state.folders.length}
					files 		= {this.state.files.length}
				/>
				<div id='content-container'>
					{
						this.state.uploadFileDialog
							? <UploadFileDialog
								closeDialog={() => this.uploadFileDialog()}
								folderPath={this.state.folderInfo.path}
								/>
							: this.state.createFolderDialog
								? <CreateFolderDialog
									closeDialog={() => this.createFolderDialog()}
									sendFolder={folder => this.sendNewFolder(folder)}
									/>
								: this.state.renameElementDialog
									? <RenameElementDialog
										element={this.state.elementSelected}
										parent={this}
										onRename={newName => this.sendRenameRequest(newName)}
										/>
									: undefined
					}
					{this.state.bufferFile !== undefined
						? <BufferElements
							/>
						: undefined}
					<ContentElements>
						{this.state.elements.map(element => this.createElement(element))}
					</ContentElements>	
					<PrevFolderButton 
						whenClicked={() => this.setState(this.state.prevState)}
						rootOpened={this.state.rootOpened}
					/>
					<button 
						className='create-folder' 
						onClick={() => this.createFolderDialog()}
					>
						<i className='fas fa-folder-plus'></i>
					</button>
					<button 
						className='upload-file-button'
						onClick={() => this.uploadFileDialog()}
					>
						<i className='fas fa-cloud-upload-alt'></i>
					</button>
				</div>
			</main>
		);
	}
}
