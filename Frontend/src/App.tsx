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
import FolderZippingNotification        from './components/main/panel/rightpanel/FolderZippingNotification/FolderZippingNotification';
import {NotificationType}               from './components/main/panel/rightpanel/notification/Notification';
import {BrowserRouter as Router, Link, Route, Switch} from "react-router-dom";

import {FolderEntity}   from './model/entity/FolderEntity';
import {FileEntity}     from './model/entity/FileEntity';
import {Entity}         from './model/entity/Entity';
import {BufferElement}  from './model/BufferElement';
import {Notification}   from './model/Notification';
import {EntityType}     from './model/entity/EntityType';

import axios from 'axios';

const API_URL 	= 'http://localhost:8080';

type IState = {
    folders 	        : FolderEntity[],
    files 		        : FileEntity[],
    bufferElement       : BufferElement | undefined,
    elementSelected     : Entity | undefined,
    folderInfo 	        : FolderEntity,
    rootOpened 	        : boolean,
    rootMemory	        : object,
    uploadingFiles      : File[],
    notifications       : Notification[],
    [fileUploadProgress : string]: any
}

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
    };

    componentDidMount() {
        this.getRootMemory();
        this.updateFolderInfo(1);
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

    updateFolderInfo = async (folderId: number) => {
        this.getFolderData(folderId);
        this.getFolderContent(folderId);
    };

    getFolderData = (folderId = this.state.folderInfo.id) => {
        this.setState({
            elementSelected : undefined
        });

        axios.get(`${API_URL}/folder/${folderId}/data`)
            .then(response => {
                this.setState({
                    folderInfo : response.data,
                    rootOpened : response.data.root
                })
            })
            .catch(error => console.log(error));
    };

    getFolderContent = (folderId = this.state.folderInfo.id) => {
        this.setState({ elementSelected : undefined });

        axios.get(`${API_URL}/folder/${folderId}/folders`)
            .then(response => {
                this.setState({ folders : response.data }
            )})
            .catch(error => console.log(error));

        axios.get(`${API_URL}/folder/${folderId}/files`)
            .then(response => {
                this.setState({ files : response.data })
            })
            .catch(error => console.log(error));
    };

    handleContextMenuAction = async (action: string, target: Entity) => {
        switch (action) {
            case 'download':
                this.sendDownloadRequest(target);
                break;
            case 'cut':
                this.setState({
                    bufferElement: {
                        action: 'cut',
                        data: target
                    }
                });
                break;
            case 'copy':
                this.setState({
                    bufferElement: {
                        action: 'copy',
                        data: target
                    }
                });
                break;
            case 'upload-files':
                const input = document.getElementById("select-upload-files");

                if (input !== null) input.click();
                break;
            case 'delete-all':
                console.log(action);
                break;
            case 'paste':
                this.sendPasteAction({
                    action: action,
                    data: target
                });
                break;
            case 'delete':
                this.sendDeleteRequest(target);
                break;
            default:
                break;
        }
    };

    addNotification = async (data: Notification) => {
        const notifications = this.state.notifications;

        notifications.push(data);

        this.setState({ notifications : notifications });
    };

    sendDownloadRequest = async (target: Entity) => {
        const targetType = target.type;
        const targetName = target.name;

        this.addNotification({
            key         : target.path,
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

    updateNotificationProcessing = (target: Entity) => {
        const notifications = this.state.notifications;

		notifications.filter(__ => __.key === target.path)[0].processing = false;

        this.setState({ notifications : notifications });
    };

    sendNewFolder = (folder: FolderEntity) => {
        axios.post(`${API_URL}/folder/create/`,
            {
                'name' 		: folder,
                'folderPath': this.state.folderInfo.path
            })
            .then(response => {
                if (response.data === 'OK') {
                    this.updateFolderInfo(this.state.folderInfo.id);
                } else {
                    console.log(response.data);
                }
            })
            .catch(error => console.log(error));
    };

    sendPasteAction = (targetEntity = this.state.bufferElement) => {
        if (targetEntity !== undefined) {
            axios.post(`${API_URL}/${targetEntity.data.type.toLowerCase()}/${targetEntity.action}`,
                {
                    oldPath: targetEntity.data.path,
                    newPath: this.state.folderInfo.path
                })
                .then(response =>
                    response.data === 'OK'
                        ? this.updateFolderInfo(this.state.folderInfo.id)
                        : console.log(response.data)
                )
                .catch(error => console.log(error));
        }
    }

    sendRenameRequest = (newName: string) => {
        const renameTarget 	= this.state.elementSelected;
        const targetType   	= renameTarget !== undefined ? renameTarget.type.toLowerCase() : "";

        if (renameTarget !== undefined) {
            axios.post(`${API_URL}/${targetType}/rename`,
                {
                    'path': renameTarget.path,
                    'newName': newName
                })
                .then(response => {
                    if (response.data === 'OK') {
                        this.updateFolderInfo(this.state.folderInfo.id);
                    }
                })
                .catch(error => console.log(error));
        }
    };

    sendDeleteRequest = (target: Entity) => {
        axios.post(`${API_URL}/${target.type.toLowerCase()}/delete`,
            {
                'path' : target.path
            })
            .then(response => response.data === 'OK'
                ? this.updateFolderInfo(this.state.folderInfo.id)
                : console.log(response.data))
            .catch(error => console.log(error));
    };

    uploadFiles = (files: Array<File>) => {
        for(let file of files) {
           this.uploadFile(file);
        }
    };

    uploadFile = (file: File) => {
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
            .then(response => response.data === 'OK'
                    ? this.updateFolderInfo(this.state.folderInfo.id)
                    : console.log(response.data))
            .catch(error => console.log(error));
    };

    createFolder() {
        return <CreateFolderDialog
                parent={this}
                sendFolder={(folder: FolderEntity) => this.sendNewFolder(folder)}
                />;
    }

    renameDialog() {
        return this.state.elementSelected !== undefined
                ? <RenameElementDialog
                    element={this.state.elementSelected}
                    onRename={(newName: string) => this.sendRenameRequest(newName)}
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
                            whenClicked={() => {
                                if (this.state.folderInfo.parentId !== null) {
                                    this.updateFolderInfo(this.state.folderInfo.parentId)
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

                    if (rightPanel !== null)
                        rightPanel.style.right = '-300px';
                }}>
                	{this.state.notifications.map(notification => 
                		<FolderZippingNotification key={notification.key} folderName={notification.folderName} processing={notification.processing}/>)}
                </RightPanel>
                <input
                    id="select-upload-files"
                    type="file"
                    onChange={() => {
                        const filesDom = document.getElementById("select-upload-files") as HTMLInputElement;

                        const files = filesDom.files;

                        if(files !== null)
                            files.length > 1 ? this.uploadFiles(Array.from(files)) : this.uploadFile(files[0]);
                    }}
                    style={{ display : 'none' }}
                    multiple/>
            </div>
            </Router>
        );
    }
}