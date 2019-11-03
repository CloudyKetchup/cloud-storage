import React from 'react';

import { Link }							from "react-router-dom";
import { FileEntity }					from '../../model/entity/FileEntity';
import EntityComponent, { EntityProps, EntityState } from '../EntityComponent/EntityComponent';
import {FileExtensionIcons}				from "./FileExtensionIcons";
import { APIHelpers as API, API_URL } 	from '../../helpers';
import App, { AppContentContext } 			from '../../App';
import CircularProgress from "@material-ui/core/CircularProgress";
import { Entity } from '../../model/entity/Entity';
import EntityContextMenu, { ContextMenuItem } from '../EntityContextMenu/EntityContextMenu';

const contextMenuListener = async (e: MouseEvent, obj: File) => {
	e.preventDefault();

	obj.setState({
		contextMenuShow: true,
		contextMenuStyle: {
			top: `${e.clientY - 20}`,
			left: `${e.clientX - 72}`
		}
	});
	obj.props.parent.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
};

const windowClickListener = async (obj: File) => {
	obj.setState({ contextMenuShow : false });

	obj.props.parent.setState({ disableContextMenu : false });
};

interface FileProps extends EntityProps { data : FileEntity }

interface FileState extends EntityState {
	imageLoaded : boolean
}

export default class File extends EntityComponent<FileProps, FileState> {
	state : FileState = {
		imageLoaded : false,
	    contextMenuShow : false,
		contextMenuStyle : {
	    	top : "",
			left : ""
		}
	};

	componentDidMount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.addEventListener('contextmenu', e => contextMenuListener(e, this));
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.removeEventListener('contextmenu', e => contextMenuListener(e, this));
	};

	moveToTrash = () => {
		API.moveToTrash(this.props.data)
			.then(response => {
				if (response === "OK") {
					this.props.mainParent.updateFolderInfo();

					API.getTrashItems().then(AppContentContext.setTrashItems);
				}
			});
	};

	imagePreloader = () => (
		<div 
			key={this.props.data.id}
			style={{
				height: "40px",
				lineHeight: "110px"
			}}>
			<CircularProgress style={{ color : "#F32C2C" }}/>
		</div>
	);

	contextMenu = (data: Entity, handleAction: (action: string) => void, app: App) => (
		this.state.contextMenuShow
		&&
		<EntityContextMenu
			parent={data}
			action={handleAction}
			onStart={() => app.setState({ elementSelected : data })}
			style={this.state.contextMenuStyle}
		>
			<Link to={`/file/image/${this.props.data.id}/view`}>
				<ContextMenuItem
					key={`context-menu-item-${this.props.data.id}`}
					icon={<i className="fas fa-eye"></i>}
					text="View"
				/>
			</Link>
		</EntityContextMenu>
	);

	render = () => (
		<div
			className="entity"
			key={this.props.data.path}
			id={`file-${this.props.data.id}`}
			style={{ height : "unset" }}
		>
			{this.contextMenu(this.props.data, this.props.handleAction, this.props.mainParent)}
			<div className="file-icon">
				{this.props.data.extension === "IMAGE_JPG"
					? [
						!this.state.imageLoaded
						&&
						this.imagePreloader(),
						<img
							key={this.props.data.path}
							style={{ display : this.state.imageLoaded ? "unset" : "none" }}
							onLoad={() => this.setState({ imageLoaded : true } as FileState)}
							src={`${API_URL}/file/${this.props.data.id}/thumbnail`}
							alt="..."/>]
					: <i className={FileExtensionIcons[this.props.data.extension as any]}/>}
			</div>
			<div className="file-footer">
				<div className="file-name">
					<span>{this.name(this.props.data.name)}</span>
				</div>
				<div className="file-footer-control">
					<button onClick={this.moveToTrash}><i className="far fa-trash-alt"/></button>
					<div style={{width: '2px', height: '60%', background: "gray", marginTop: "7%"}}/>
					<Link 
						onClick={() => this.props.mainParent.setState({ elementSelected : this.props.data })}
						to={`/file/${this.props.data.id}/info`}
						style={{ lineHeight : "25px", color : "#181818", textAlign : "center"}}>
						<i className="fas fa-info-circle"/>
					</Link>
				</div>
			</div>	
		</div>
	);
}
