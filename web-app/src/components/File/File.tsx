import React from 'react';

import { Link }											from 'react-router-dom';
import { FileEntity }									from '../../model/entity/FileEntity';
import EntityComponent, { EntityProps, EntityState }	from '../EntityComponent/EntityComponent';
import { FileExtensionIcons }							from './FileExtensionIcons';
import { API_URL } 										from '../../helpers';
import EntityContextMenu, { ContextMenuItem } 			from '../EntityContextMenu/EntityContextMenu';
import CircularProgress 								from '@material-ui/core/CircularProgress';

const contextMenuListener = async (e: MouseEvent, obj: File) => {
	e.preventDefault();

	obj.setState({
		contextMenuShow: true,
		contextMenuStyle: {
			top: `${e.clientY - 20}`,
			left: `${e.clientX - 72}`
		}
	});

	window.addEventListener('click', () => {
		const contextMenu = document.getElementById(`entity-${obj.props.data.id}-context-menu`);

		if (contextMenu && contextMenu.style) {
			contextMenu.style.marginLeft = "75px";
			contextMenu.style.opacity = "0";
		}
		setTimeout(() => windowClickListener(obj), 100);
	});

	obj.props.container.setState({ disableContextMenu : true });
};

const windowClickListener = async (obj: File) => {
	obj.setState({ contextMenuShow : false });

	obj.props.container.setState({ disableContextMenu : false });
};

interface FileProps extends EntityProps { data : FileEntity }

interface FileState extends EntityState { imageLoaded : boolean }

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

	contextMenu = () => (
		this.state.contextMenuShow
		&&
		<EntityContextMenu
			parent={this.props.data}
			action={this.props.handleAction}
			onStart={() => this.props.mainParent.setState({ elementSelected : this.props.data })}
			style={this.state.contextMenuStyle}
		>
			<Link to={`/file/image/${this.props.data.id}/view`}>
				<ContextMenuItem
					key={`context-menu-item-${this.props.data.id}`}
					icon={<i className="fas fa-eye"/>}
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
			{this.contextMenu()}
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
					<button onClick={() => this.props.mainParent.moveToTrash(this.props.data)}>
						<i className="far fa-trash-alt"/>
					</button>
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
