import React, { Component } from 'react';

import Nav 						from './components/Nav'
import SideBar 					from './components/SideBar';
import ContentContainer 		from './components/ContentContainer';
import PrevFolderButton 		from './components/PrevFolderButton';
import BufferElementIndicator 	from './components/BufferElementIndicator';
import ElementInfoContainer 	from './components/ElementInfoContainer';

const axios = require('axios');

const API_URL 	= 'http://localhost:8080';
const root 		= 'cloud';

export default class App extends Component {
	constructor() {
		super();
		this.state = {
			elements 	: [],
			folders 	: [],
			files 		: [],
			bufferElement   : undefined,
			elementInfoContainer : false,
			elementSelected : undefined,
			folderInfo 	: this.folderInfo,
			rootOpened 	: true,
			rootMemory	: {},
			renameElementDialog : false,
			createFolderDialog 	: false,
			uploadFileDialog 	: false
		};

		window.onkeyup = e => {
			const key = e.keyCode ? e.keyCode : e.which;

			if (key === 27) {
				this.toggleDialogs();
				this.toggleElementInfo();
			}
		}
	}

	componentDidMount() {
		this.getRootContent();
		this.getRootMemory();		
	}

	getRootContent() {
		axios.get(`${API_URL}/folder/root/content`)
			.then(root => this.setState({
					folders 	: root.data.folders,
					files 		: root.data.files,
					elements 	: this.sortElements(root.data.folders.concat(root.data.files)),
					folderInfo 	: root.data.rootFolder
				}))
			.catch(error => console.log(error));
	}

	getRootMemory() {
		axios.get(`${API_URL}/folder/root/memory`)
			.then(memory => 
				this.setState({
					rootMemory : memory.data,
					rootOpened : true
				})
			)
			.catch(error => console.log(error));
	}

	updateElementsData = (folderId = this.state.folderInfo.id) => {
		this.setState({ elementSelected : undefined });

		axios.get(`${API_URL}/folder/${folderId}/content`)
			.then(content => 
				this.setState({
					folders 	: content.data.folders,
					files 		: content.data.files,
					elements 	: this.sortElements(content.data.folders.concat(content.data.files)),
					folderInfo 	: content.data.folderInfo,
					rootOpened 	: content.data.folderInfo.root
				})
			)
			.catch(error => console.log(error));
	};

	handleContextMenuAction(action, element) {
		switch(action) {
			case 'cut':
				this.setState({
					bufferElement : {
						action  : 'cut',
						data 	: element
					}
				});
				break;
			case 'copy':
				this.setState({
					bufferElement : {
						action  : 'copy',
						data 	: element
					}
				});
				break;
			case 'rename':
				this.toggleDialogs(true, false, false);
				break;
			case 'upload-files':
				this.toggleDialogs(false, false, true);
				break;
			case 'create-folder':
				this.toggleDialogs(false, true, false);
				break;
			case 'delete-all':
				console.log(action)
				break;
			case 'paste':
				this.sendPasteAction(element, action);
				break;
			case 'delete':
				this.sendDeleteRequest(element);
				break;
			case 'info':
				this.toggleElementInfo(true);
				break;
			default: break;
		}
	}

	toggleDialogs(
		renameElementDialog = false, 
		createFolderDialog 	= false,
		uploadFileDialog 	= false
	) {
		this.setState({
			'renameElementDialog' : renameElementDialog,
			'createFolderDialog'  : createFolderDialog,
			'uploadFileDialog' 	  : uploadFileDialog
		});
	}

	sendNewFolder(folder) {
		axios.post(`${API_URL}/folder/create/`,
			{
				'name' 		: folder,
				'folderPath': this.state.folderInfo.path
			})
			.then(response => {
				if (response.data === 'OK') {
					
					this.setState({ createFolderDialog : false });

					this.state.folderInfo.name === root ? this.getRootContent() : this.updateElementsData();
				}
			})
			.catch(error => console.log(error));
	}

	sendPasteAction(
		element = this.state.bufferElement, 
		action, 
		path 	= this.state.folderInfo.path
	) {
		axios.post(`${API_URL}/${element.data.type.toLowerCase()}/${element.action}`,
			{
				oldPath : element.data.path,
				newPath : path
			})
			.then(response => this.state.folderInfo.name === root ? this.getRootContent() : this.updateElementsData())
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
				if (response.data === 'OK') {
					this.setState({ renameElementDialog : false });
					
					this.state.folderInfo.name === root ? this.getRootContent() : this.updateElementsData();
				}
			})
			.catch(error => console.log(error));
	}

	sendDeleteRequest(element) {
		axios.post(`${API_URL}/folder/delete`,
			{
				[`${element.type.toLowerCase()}Path`] : element.path
			})
			.then(response => response.data === 'OK'
					? this.state.folderInfo.name === root
						? this.getRootContent()
						: this.updateElementsData(this.state.folderInfo.id)
					: console.log(response.data))
			.catch(error => console.log(error));
	}

	sendDeleteAllFolderContent() {
		axios.post(`${API_URL}/folder/deleteAllContent`,
			{	
				'folderPath' : this.state.folderInfo.path
			})
			.then(response => response.data === 'OK'
				? this.state.folderInfo.name === root
					? this.getRootContent()
					: this.updateElementsData(this.state.folderInfo.id)
				: undefined)
			.catch(error => console.log(error));
	}

	toggleElementInfo(toggle = false)  {
		this.setState({ elementInfoContainer : toggle });
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

	bufferElementIndicator() {
		if (this.state.bufferElement !== undefined) {
			return <BufferElementIndicator
				element={this.state.bufferElement}
			/>
		}
	}

	elementInfoContainer() {
		if (this.state.elementSelected !== undefined && this.state.elementInfoContainer) {
			 return <ElementInfoContainer
					parent={this}
					data={this.state.elementSelected}
					/>
		}
	}

	render() {
		return (
			<main style={{height : '100%'}}>
				<Nav>
					{this.bufferElementIndicator()}
				</Nav>
				<SideBar
					memory 		= {this.state.rootMemory} 
					folderInfo 	= {this.state.folderInfo}
					folders 	= {this.state.folders.length}
					files 		= {this.state.files.length}
				/>
				<ContentContainer
					parent={this}
					elementsData={this.state.elements}
					>
					<PrevFolderButton
						whenClicked = {() => this.updateElementsData(this.state.folderInfo.parentId)}
						rootOpened  = {this.state.rootOpened}
					/>
					{this.elementInfoContainer()}
					<button 
						className = 'create-folder' 
						onClick   = {() => this.toggleDialogs(false, true, false)}
					>
						<i className='fas fa-folder-plus'/>
					</button>
					<button 
						className = 'upload-file-button'
						onClick   = {() => this.toggleDialogs(false, false, true)}
					>
						<i className='fas fa-file-upload'/>
					</button>
				</ContentContainer>
			</main>
		);
	}
}
