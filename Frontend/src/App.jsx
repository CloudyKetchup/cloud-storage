import React, { Component } from 'react';

import NavBar 						from './components/main/navbar/NavBar'
import LeftPanel 					from './components/main/panel/leftpanel/LeftPanel';
import ContentContainer 			from './components/content/container/ContentContainer';
import PrevFolderButton 			from './components/content/container/control/PrevFolderButton';
import BufferElementIndicator 		from './components/bufferelement/BufferElementIndicator';
import { ElementInfoContainer } 	from './components/content/elements/info/ElementInfoContainer';
import DragAndDrop              	from './components/content/dragdrop/DragAndDrop';
import FileUploadManager        	from './components/content/upload/UploadManager';
import { UploadFile }           	from './components/content/upload/UploadManager';
import RightPanel               	from './components/main/panel/rightpanel/RightPanel';
import CreateFolderDialog       	from './components/content/container/control/CreateFolderDialog';
import RenameElementDialog 			from './components/content/elements/rename/RenameElementDialog';
import FolderZippingNotification 	from './components/main/panel/rightpanel/FolderZippingNotification/FolderZippingNotification';

import { BrowserRouter as Router, Route, Switch, Link } from "react-router-dom";

import axios from 'axios';
import { NotificationType } from './components/main/panel/rightpanel/notification/Notification';

const API_URL 	= 'http://localhost:8080';

export default class App extends Component {
    constructor() {
        super();
        this.state = {
            folders 	            : [],
            files 		            : [],
            bufferElement           : undefined,
            elementSelected         : undefined,
            folderInfo 	            : this.folderInfo,
            rootOpened 	            : true,
            rootMemory	            : {},
            uploadingFiles          : [],
            notifications           : []
        };

        window.onkeyup = e => {
            const key = e.keyCode ? e.keyCode : e.which;

            if (key === 27) {
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

    handleContextMenuAction = (action, target) => {
        switch(action) {
            case 'download':
                this.sendDownloadRequest(target);
                break;
            case 'cut':
                this.setState({bufferElement : {
                        action  : 'cut',
                        data 	: target
                }});
                break;
            case 'copy':
                this.setState({bufferElement : {
                        action  : 'copy',
                        data 	: target
                }});
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
                this.sendPasteAction(target, action);
                break;
            case 'delete':
                this.sendDeleteRequest(target);
                break;
            default: break;
        }
    };

    addNotification = async data => {
        const notifications = this.state.notifications;

        notifications.push(data);

        this.setState({ notifications : notifications });
    };

    sendDownloadRequest = async target => {
        const targetType = target.type;
        const targetName = target.name;

        this.addNotification({
            id 			: target.path,
            type 		: NotificationType.processing,
            message 	: `Processing "${target.name}"`,
            targetType 	: targetType,
            folderName 	: target.name,
            processing 	: true
        });

        axios.request({
                url: `${API_URL}/${target.type.toLowerCase()}/${target.id}/download`,
                method: 'GET',
                responseType: 'blob',
                onDownloadProgress : p => {
                    if (((p.total - (p.total - p.loaded)) / p.total * 100) < 100) {
                        this.updateNotificationProcessing(target);
                    }
                }
            })
            .then(response => {
                const url = window.URL.createObjectURL(new Blob([response.data]));
                
                const link = document.createElement('a');
                
                link.href = url;
                
                link.setAttribute('download', targetType === 'FILE' ? targetName : `${targetName}.zip`);
                
                document.body.appendChild(link);
                
                link.click();
            })
            .catch(error => console.log(error));
    };

    updateNotificationProcessing = target => {
        const notifications = this.state.notifications;

		notifications.filter(__ => __.id === target.path)[0].processing = false;

        this.setState({ notifications : notifications });
    };

    sendNewFolder = folder => {
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

        axios.post(`${API_URL}/${targetType}/rename`,
            {
                'path' : renameTarget.path,
                'newName' : newName
            })
            .then(response => {
                if (response.data === 'OK') {
                    this.updateFolderInfo();
                }
            })
            .catch(error => console.log(error));
    };

    sendDeleteRequest = target => {
        axios.post(`${API_URL}/${target.type.toLowerCase()}/delete`,
            {
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

    createFolder() {
        return <CreateFolderDialog
                parent={this}
                sendFolder={folder => this.sendNewFolder(folder)}
                />;
    }

    renameDialog() {
        return this.state.elementSelected !== undefined
                ? <RenameElementDialog
                    element={this.state.elementSelected}
                    onRename={newName => this.sendRenameRequest(newName)}
                    />
                : null;
    }

    elementInfoContainer() {
        return this.state.elementSelected !== undefined
                ? <ElementInfoContainer parent={this} data={this.state.elementSelected}/>
                : null;
    }

    render() {
        return (
            <Router>
            <div style={{height : '100%'}}>
                <NavBar/>
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
                            <Route path="/create-folder" component={() => this.createFolder()}/>
                            <Route path="/element-info" component={() => this.elementInfoContainer()}/>
                        </Switch>
                        <PrevFolderButton
                            whenClicked={() => this.updateFolderInfo(this.state.folderInfo.parentId)}
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
                            onClick={() => document.getElementById('select-upload-files').click()}
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
                <RightPanel closePanel={() => document.getElementById("right-panel").style.right = '-300px'}>
                	{this.state.notifications.map(notification => 
                		<FolderZippingNotification key={notification.path} folderName={notification.folderName} processing={notification.processing}/>)}
                </RightPanel>
                <input
                    id="select-upload-files"
                    type="file"
                    onChange={() => {
                        const files = document.getElementById("select-upload-files").files;

                        files.length > 1 ? this.uploadFiles(files) : this.uploadFile(files[0]);
                    }}
                    style={{ display : 'none' }}
                    multiple/>
            </div>
            </Router>
        );
    }
}