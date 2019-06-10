import React, { Component } from 'react';

import Nav 						from './components/Nav'
import SideBar 					from './components/SideBar';
import ContentContainer 		from './components/ContentContainer';
import PrevFolderButton 		from './components/PrevFolderButton';
import BufferElementIndicator 	from './components/BufferElementIndicator';
import ElementInfoContainer 	from './components/ElementInfoContainer';

const axios = require('axios');

const API_URL = 'http://localhost:8080'

export default class App extends Component {
	constructor() {
		super();
		this.state = {
			elements 	: [],
			folders 	: [],
			files 		: [],
			bufferElement   : undefined,
			elementInfo : undefined,
			elementSelected : undefined,
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
			case 'delete':
				this.sendDeleteRequest(element);
				break;
			case 'info':
				this.toggleElementInfo(element);
				break;
			default: break;
		}
	}

	handleContainerContextMenu(action, folder) {
		switch(action) {
			case 'delete-all':
				break;
			case 'paste':
				break;
			default: break;
		}
	}

	sendContextMenuAction(URL) {
		axios.post(URL)
			.then(response => console.log(response.data))
			.catch(error => console.log(error));
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
					
					this.folderInfo.name !== 'cloud' ? this.updateElementsData() : this.getRootContent();

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
					
					this.state.folderInfo.name === 'cloud' ? this.getRootContent() : this.updateElementsData();
				}
			})
			.catch(error => console.log(error));
	}

	sendDeleteRequest(element) {

		axios.post(`${API_URL}/folder/delete`,
			{
				[`${element.type.toLowerCase()}Path`] : element.path
			})
			.then(response => console.log(response.data))
			.catch(error => console.log(error));
	}

	toggleElementInfo(element)  {
		this.setState({ elementInfo : !this.state.elementInfo });
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
				<Nav>
					{this.state.bufferElement !== undefined
						? <BufferElementIndicator
							element={this.state.bufferElement}
							/>
						: undefined}
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
						whenClicked = {() => this.setState(this.state.prevState)}
						rootOpened  = {this.state.rootOpened}
					/>
					{this.state.elementInfo !== undefined
						? <ElementInfoContainer
							parent={this}
							data={this.state.elementSelected}
							/>
						: undefined}
					<button 
						className = 'create-folder' 
						onClick   = {() => this.toggleDialogs(false, true, false)}
					>
						<i className='fas fa-folder-plus'></i>
					</button>
					<button 
						className = 'upload-file-button'
						onClick   = {() => this.toggleDialogs(false, false, true)}
					>
						<i className='fas fa-cloud-upload-alt'></i>
					</button>
				</ContentContainer>
			</main>
		);
	}
}
