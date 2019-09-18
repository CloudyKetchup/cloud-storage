import React from 'react';

import {Link}							from "react-router-dom";
import {FileEntity}						from '../../../../model/entity/FileEntity';
import EntityComponent, {EntityProps} 	from '../entity/EntityComponent';
import {FileExtensionIcons}				from "./FileExtensionIcons";

const contextMenuListener = async (e: MouseEvent, obj: File) => {
	e.preventDefault();

	obj.setState({
		contextMenuShow  : true,
		contextMenuStyle : {
			top : e.y - 70,
			left: e.x - 275
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

export default class File extends EntityComponent<FileProps> {

	componentDidMount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.removeEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

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
					? <img src={`http://localhost:8080/file/${this.props.data.path.replace(/[/]/g, '%2F')}/image`} alt=""/>
					: <i className={FileExtensionIcons[this.props.data.extension as any]}/>}
			</div>
			<div className="file-footer">
				<div className="file-name">
					<span>{this.name(this.props.data.name)}</span>
				</div>
				<div className="file-footer-control">
					<button onClick={() => this.props.mainParent.sendDeleteRequest(this.props.data)}><i className="far fa-trash-alt"/></button>
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
