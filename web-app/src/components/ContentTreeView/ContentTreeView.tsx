import React, { Component } from 'react';
import ReactDOM 			from "react-dom";

import App						from '../../App';
import { APIHelpers as API }	from '../../helpers';
import { Entity }				from "../../model/entity/Entity";
import { EntityType }			from '../../model/entity/EntityType';
import { FileEntity }			from '../../model/entity/FileEntity';
import { FolderEntity }			from '../../model/entity/FolderEntity';
import EntityContextMenu 		from '../EntityContextMenu/EntityContextMenu';

const cutNames = (nodes : Entity[]) : Entity[] => {
	nodes.forEach(node => {
		node.name = node.name.length > 16 ? `${node.name.substring(0, 15)}...` : node.name;
	});
	return nodes;
};

const contextMenuListener = async (e: MouseEvent, obj: TreeNode) => {
	e.preventDefault();

	obj.setState({
		contextMenuShow: true,
		contextMenuStyle: {
			top: `${e.clientY - 20}`,
			left: `${e.clientX - 72}`,
			border: "1px solid"
		}
	});
	obj.props.app.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
};

const windowClickListener = async (obj: TreeNode) => {
	obj.setState({ contextMenuShow : false });

	obj.props.app.setState({ disableContextMenu : false });
};

type TreeNodeState = {
    id      : string
    name    : string
	type 	: EntityType
    toggled : boolean
	files 	: FileEntity[]
	folders : FolderEntity[]
	contextMenuShow  : boolean
	contextMenuStyle : any
};

class TreeNode extends Component<{ data : Entity, app : App }, TreeNodeState> {
    state : TreeNodeState = {
        id      : this.props.data.id,
        name    : this.props.data.name,
        type    : this.props.data.type,
        toggled : false,
		files 	: [],
		folders : [],
		contextMenuShow : false,
		contextMenuStyle : {} 
    };

	UNSAFE_componentWillMount() {
		const div = ReactDOM.findDOMNode(this);

		if (div !== null) {
			div.addEventListener("contextmenu", async e => {
				e.preventDefault();

				await contextMenuListener(e as MouseEvent, this)
			});
		}
	}

    icon = () => {
		const style = {
			fontSize : 20,
			lineHeight : "40px",
			paddingLeft : 5
		};

        if (this.state.type === EntityType.FOLDER) {
            if (this.state.toggled) {
                return <i className="fas fa-folder-open" style={style}/>
            }
            return <i className="fas fa-folder" style={style}/>
        }
        return <i className="fas fa-file" style={style}/>
    };

	name = () => cutNames([this.props.data])[0].name;

    onToggle = async () => {
		const arrow = document.getElementById(`${this.state.id}-tree-node-arrow`);

        if (this.state.toggled) {
			if (arrow !== null) {
				arrow.style.transform = "unset";
			}
            this.setState({ toggled : false });
        } else {
			this.setState({
				toggled : true,
				folders : cutNames(await API.getFolderFolders(this.state.id)) as FolderEntity[],
				files	: cutNames(await API.getFolderFiles(this.state.id)) as FileEntity[]
			});

			if (arrow !== null) {
				arrow.style.transform = "rotate(90deg)"
			}
        }
    };

    render = () => (
        <div className="content-tree-node">
			{this.state.contextMenuShow
			&&
			<EntityContextMenu
				parent={this.props.data}
				onStart={() => this.props.app.setState({ elementSelected : this.props.data })}
				action={async (action : string) => {
					this.props.app.setState({ elementSelected : this.props.data });

					await this.props.app.handleContextMenuAction(action, this.props.data);
				}}
				style={this.state.contextMenuStyle}
			/>}
			<div style={{ display : "flex", width : 200 }}>
				{this.state.type === EntityType.FOLDER
				&&
				<i
					onClick={this.state.type === EntityType.FOLDER ? () => this.onToggle() : undefined}
					id={`${this.state.id}-tree-node-arrow`}
					className="tree-node-arrow fas fa-chevron-right" style={{ width : 20, fontSize : 12, lineHeight : "40px" }}/>}
			<div 
				className="content-tree-node-self"
				style={{ paddingLeft : this.state.type === EntityType.FILE ? "25px" : "unset" }}
				onClick={async () => {
					if (this.state.type === EntityType.FOLDER) {
						await this.props.app.updateFolder(this.state.id);
					}
				}}
			>
				<div className="content-tree-node-icon">
					{this.icon()}
				</div>
				<div className="content-tree-node-name">
					<span>{this.name()}</span>
					</div>
				</div>
			</div>
			{this.state.type === EntityType.FOLDER
			&&
			this.state.toggled
			&&
			<div className="content-tree-node-children">
				{this.state.folders.map(folder => <TreeNode app={this.props.app} data={folder} key={folder.id}/>)}
				{this.state.files.map(file => <TreeNode app={this.props.app} data={file} key={file.id}/>)}
			</div>}
		</div>
	);
}

export class ContentTreeView extends Component<{ app: App }> {
	state = {
		files 	: [] as FileEntity[],
		folders : [] as FolderEntity[]
	};

	UNSAFE_componentWillMount = () => this.getContent().then(content => this.setState(content));

	getContent = async () : Promise<object> => {
		const rootId = await API.getRootId();

		const files = await API.getFolderFiles(rootId);

		const folders = await API.getFolderFolders(rootId);

		return { files : files, folders : folders };
	};

	render = () => (
		<div className="content-tree-view">
			{this.state.folders.map(folder => <TreeNode data={folder} key={folder.id} app={this.props.app}/>)}
			{this.state.files.map(file => <TreeNode data={file} key={file.id} app={this.props.app}/>)}
		</div>
	)
}