import React, {Component}from 'react'

import Nav 					from './components/Nav'
import SideBar 				from './components/SideBar'
import ContentElements 		from './components/ContentElements'
import Folder 				from './components/Folder';
import File 				from './components/File';
import PrevFolderButton 	from './components/PrevFolderButton'
import UploadFileDialog 	from './components/UploadFileDialog'
import CreateFolderDialog 	from './components/CreateFolderDialog'

const axios = require('axios');

class App extends Component {
	
	constructor() {
		super();
		
		this.state = {
			elements 	: [],
			folderInfo 	: this.folderInfo,
			prevState 	: this.prevState,
			rootOpened 	: true,
			rootMemory	: {},
			createFolderDialog : false,
			uploadFile 	: false
		};
		this.updateElementsData = this.updateElementsData.bind(this);
	}

	componentDidMount() {
		this.getRootContent();
		this.getRootMemory();
	}

	getRootContent() {
		axios.get('http://localhost:8080/folder/root/content')
			.then(root =>
				this.setState({
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
		axios.get(`http://localhost:8080/folder/${key}/content`)
			.then(content => 
				this.setState(prevState => { 
					return {
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
				whenClicked={() => this.updateElementsData(element.id)}
				/>
	}

	uploadFileDialog() {
		this.setState(prevState => ({
			'uploadFile' : !prevState.uploadFile,
			'createFolderDialog' : false
		}))
	}

	createFolderDialog() {
		this.setState(prevState => ({
			'createFolderDialog' : !prevState.createFolderDialog,
			'uploadFile' : false
		}))
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
				<Nav 
					folderInfo={this.state.folderInfo} 
					folderElements={this.state.elements.length}
				/>
				<SideBar memory={this.state.rootMemory}/>
				<div id="content-container">
					{
						this.state.uploadFile
							? <UploadFileDialog
								closeDialog={() => this.uploadFileDialog()}
								folderPath={this.state.folderInfo.path}
								/>
							: this.state.createFolderDialog 
								? <CreateFolderDialog
									closeDialog={() => this.createFolderDialog()}
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

export default App;