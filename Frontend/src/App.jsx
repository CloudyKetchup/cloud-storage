import React, { Component } from 'react';

import Nav 						from './components/Nav'
import SideBar 					from './components/SideBar';
import ContentContainer 		from './components/ContentContainer';
import PrevFolderButton 		from './components/PrevFolderButton';
import BufferElementIndicator 	from './components/BufferElementIndicator';
import ElementInfoContainer 	from './components/ElementInfoContainer';
import DragAndDrop              from './components/DragAndDrop';
import FileUploadManager         from './components/FileUploadManager';
import { UploadFile }           from './components/FileUploadManager';

import axios from 'axios';

const API_URL 	= 'http://localhost:8080';

export default class App extends Component {
    constructor() {
        super();
        this.state = {
            folders 	            : [],
            files 		            : [],
            bufferElement           : undefined,
            elementInfoContainer    : false,
            elementSelected         : undefined,
            folderInfo 	            : this.folderInfo,
            rootOpened 	            : true,
            rootMemory	            : {},
            renameElementDialog     : false,
            createFolderDialog 	    : false,
            uploadingFiles          : []
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
        this.updateFolderInfo(1)
        this.getRootMemory();
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

    updateFolderInfo = (folderId = this.state.folderInfo.id) => {
        this.getFolderData(folderId);
        this.getFolderContent(folderId, 'files');
        this.getFolderContent(folderId, 'folders');
    };

    getFolderData = folderId => {
        this.setState({ elementSelected : undefined });

        axios.get(`${API_URL}/folder/${folderId}/data`)
            .then(response => {
                this.setState({
                    folderInfo : response.data,
                    rootOpened : response.data.root
                })
            })
            .catch(error => console.log(error));
    };

    getFolderContent = (folderId = this.state.folderInfo.id, element) => {
        this.setState({ elementSelected : undefined });

        axios.get(`${API_URL}/folder/${folderId}/${element}`)
            .then(response => {
                this.setState({ [`${element}`] : response.data }
            )})
            .catch(error => console.log(error));
    };

    handleContextMenuAction = (action, element) => {
        switch(action) {
            case 'cut':
                this.setState({bufferElement : {
                        action  : 'cut',
                        data 	: element
                }});
                break;
            case 'copy':
                this.setState({bufferElement : {
                        action  : 'copy',
                        data 	: element
                }});
                break;
            case 'rename':
                this.toggleDialogs(true, false, false);
                break;
            case 'upload-files':
                document.getElementById("select-upload-files").click();
                break;
            case 'create-folder':
                this.toggleDialogs(false, true, false);
                break;
            case 'delete-all':
                console.log(action);
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
    };

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

    sendNewFolder = folder =>{
        axios.post(`${API_URL}/folder/create/`,
            {
                'name' 		: folder,
                'folderPath': this.state.folderInfo.path
            })
            .then(response => {
                if (response.data === 'OK') {
                    this.updateFolderInfo();
                    this.toggleDialogs();
                } else {
                    console.log(response.data);
                }
            })
            .catch(error => console.log(error));
    };

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
            .then(response => 
                response.data === 'OK'
                    ? this.updateFolderInfo()
                    : console.log(response.data)
            )
            .catch(error => console.log(error));
    }

    sendRenameRequest = newName => {
        const renameTarget 	= this.state.elementSelected;
        const targetType   	= renameTarget.type.toLowerCase();

        axios.post(`${API_URL}/${targetType}/rename`,{
                'path' : renameTarget.path,
                'newName' : newName
            })
            .then(response => {
                if (response.data === 'OK') {
                    this.setState({ renameElementDialog : false });

                    this.updateFolderInfo()
                }
            })
            .catch(error => console.log(error));
    };

    sendDeleteRequest = target => {
        axios.post(`${API_URL}/${target.type.toLowerCase()}/delete`,{
                'path' : target.path
            })
            .then(response => response.data === 'OK'
                ? this.updateFolderInfo()
                : console.log(response.data))
            .catch(error => console.log(error));
    };

    sendDeleteAllFolderContent() {
        axios.post(`${API_URL}/folder/deleteAllContent`,
            {
                'folderPath' : this.state.folderInfo.path
            })
            .then(response => response.data === 'OK'
                ? this.updateFolderInfo()
                : undefined)
            .catch(error => console.log(error));
    }

    toggleElementInfo = (toggle = false) => {
        this.setState({ elementInfoContainer : toggle });
    }

    uploadFiles = files => {
        for(let file of files) {
           this.uploadFile(file);
        }
    };

    uploadFile = file => {
        const URL = `${API_URL}/file/upload/one`;

        const formData = new FormData();

        formData.append('file', file);
        formData.append('path', this.state.folderInfo.path);

        const uploadingFiles = this.state.uploadingFiles;

        uploadingFiles.push(file);

        this.setState({ uploadingFiles : uploadingFiles });

        axios.request({
                url     : URL,
                method  : 'POST',
                data    : formData,
                onUploadProgress : p => this.setState({
                        [`uploadingFile${file.name}progress`] : (p.total - (p.total - p.loaded)) / p.total * 100
                })
            })
            .then(response => response.data === 'OK' ? this.updateFolderInfo() : console.log(response.data))
            .catch(error => console.log(error));
    };

    render() {
        return (
            <main style={{height : '100%'}}>
                <Nav>
                    {this.state.bufferElement !== undefined
                    &&
                    <BufferElementIndicator element={this.state.bufferElement}/>}
                </Nav>
                <SideBar
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
                        <PrevFolderButton
                            whenClicked={() => this.updateFolderInfo(this.state.folderInfo.parentId)}
                            rootOpened={this.state.rootOpened}
                        />
                        {this.state.elementSelected !== undefined
                        &&
                        this.state.elementInfoContainer
                        &&
                        <ElementInfoContainer parent={this} data={this.state.elementSelected}/>}
                        <button
                            className='create-folder'
                            onClick={() => this.toggleDialogs(false, true, false)}
                        >
                            <i className='fas fa-folder-plus'/>
                        </button>
                        <button
                            className='upload-file-button'
                            onClick={() => document.getElementById('select-upload-files').click()}
                        >
                            <i className='fas fa-file-upload'/>
                        </button>
                        {this.state.uploadingFiles.length > 0
                        &&
                        <FileUploadManager onClose={() => this.setState({ uploadingFiles : [] })}>
                            {this.state.uploadingFiles.map(file => <UploadFile key={file.name} name={file.name} data={file} parent={this}/>)}
                        </FileUploadManager>}
                    </ContentContainer>
                </DragAndDrop>
                <input
                    id="select-upload-files"
                    type="file"
                    onChange={() => {
                        const files = document.getElementById("select-upload-files").files;

                        files.length > 1 ? this.uploadFiles(files) : this.uploadFile(files[0]);
                    }}
                    style={{ display : 'none' }}
                    multiple/>
            </main>
        );
    }
}