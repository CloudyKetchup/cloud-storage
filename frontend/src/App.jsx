import React, {Component} from 'react';

import Nav 					from './components/Nav'
import SideBar 				from './components/SideBar';
import ContentElements 		from './components/ContentElements';
import Folder 				from './components/Folder';
import File 				from './components/File';
import PrevFolderButton 	from './components/PrevFolderButton';
import UploadFileDialog 	from './components/UploadFileDialog';
import CreateFolderDialog 	from './components/CreateFolderDialog';

const axios = require('axios');

export default class App extends Component {
	
	constructor() {
		super();
		this.state = {
			elements 	: [],
			folders 	: [],
			files 		: [],
			elementSelected : undefined,
			folderInfo 	: this.folderInfo,
			prevState 	: this.prevState,
			rootOpened 	: true,
			rootMemory	: {},
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
		this.updateElementsData = this.updateElementsData.bind(this);
	}

	componentDidMount() {
		this.getRootMemory();
		this.getRootContent();
	}

	getRootContent() {
		axios.get('http://localhost:8080/folder/root/content')
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
		axios.get('http://localhost:8080/folder/root/memory')
			.then(memory => 
				this.setState({
					rootMemory : memory.data
				})
			)
			.catch(error => console.log(error));
	}

	updateElementsData(key) {
		this.setState({ elementSelected : undefined });
		axios.get(`http://localhost:8080/folder/${key}/content`)
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
				whenClicked={() => 
						this.state.elementSelected !== undefined && this.state.elementSelected.id === element.id
							? this.updateElementsData(element.id)
							: this.setState({ elementSelected : element})
				}/>
	}

	uploadFileDialog() {
		this.setState(prevState => ({
			'uploadFileDialog' 	: !prevState.uploadFileDialog,
			'createFolderDialog': false
		}));
	}

	createFolderDialog() {
		this.setState(prevState => ({
			'createFolderDialog': !prevState.createFolderDialog,
			'uploadFileDialog'	: false
		}));
	}

	sendNewFolder(folder) {
		axios.post('http://localhost:8080/folder/create/',{
				'name' 		: folder,
				'folderPath': this.state.folderInfo.path
			})
			.then(response => {
				if (response.data === "OK") {
					
					this.updateElementsData(this.state.folderInfo.id);

					this.setState({ createFolderDialog : false });
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
			<div style={{height : '100%'}}>
				<Nav />
				<SideBar 
					memory 		= {this.state.rootMemory} 
					folderInfo 	= {this.state.folderInfo}
					folders 	= {this.state.folders.length}
					files 		= {this.state.files.length}
				/>
				<div id="content-container">
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
								: undefined
					}
					<ContentElements>
						{this.state.elements.map(element => this.createElement(element))}
					</ContentElements>	
					<PrevFolderButton 
						whenClicked={() => this.setState(this.state.prevState)}
						rootOpened={this.state.rootOpened}
					/>
					<button 
						className="create-folder" 
						onClick={() => this.createFolderDialog()}
					>
						<i className="fas fa-folder-plus"></i>
					</button>
					<button 
						className="upload-file-button"
						onClick={() => this.uploadFileDialog()}
					>
						<i className="fas fa-cloud-upload-alt"></i>
					</button>
				</div>
			</div>
		);
	}
}
