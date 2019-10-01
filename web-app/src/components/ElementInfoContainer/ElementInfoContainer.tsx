import React, { Component } from 'react';

import App 				from '../../App';
import { Link, match } 	from "react-router-dom";
import { FileEntity } 	from '../../model/entity/FileEntity';
import { FolderEntity } from '../../model/entity/FolderEntity';
import { EntityType } 	from '../../model/entity/EntityType';
import { APIHelpers } 	from '../../helpers';

interface IProps {
	parent : App
	match : match<{ id : string, type : string }>
}

interface IState {  data : FileEntity | FolderEntity }

export class ElementInfoContainer extends Component<IProps, IState> {
	state : IState = {
		 data : {
			id : "",
			name : "",
			path : "",
			location : "",
			timeCreated : "",
			size : "",
			extension : "",
			type : EntityType.FILE
		}
	}

	componentDidMount() {
		const entityType = this.props.match.params.type;
		const functionName = `get${entityType[0].toUpperCase() + entityType.slice(1)}Data`;

		if (functionName === "getFileData" || functionName === "getFolderData")
			APIHelpers[functionName](this.props.match.params.id)
				.then(result => {
					if ((result as FileEntity) !== undefined)
						this.setState({  data : result as FileEntity });
					else if ((result as FolderEntity) !== undefined)
						this.setState({  data : result as FolderEntity });
				})
	}
	
	render = () => (
		<div className="element-info-container">
			<div className="dialog-header" style={{
				position: 'absolute',
				height: '40px',
				top: 0,
				right: 0,
				left: 0
			}}>
				<Link to="/">
					<button
						className="prev-button"
					>
						<i className="fas fa-chevron-left"/>
					</button>
				</Link>
				<span>{this.state.data.type.toLowerCase()} info</span>
			</div>
			<div className="element-info" style={{
				height: 'calc(100% - 60px)',
				width: '95%',
				bottom: '0px'
			}}>
				<div>
					<span className="description-text">Name</span>
					<span className="element-info-text">{this.state.data.name}</span>
				</div>
				<div>
					<span className="description-text">Location</span>
					<span className="element-info-text">{this.state.data.location}</span>
				</div>
				<div>
					<span className="description-text">Time created</span>
					<span className="element-info-text">{this.state.data.timeCreated}</span>
				</div>
				<div>
					<span className="description-text">Size</span>
					<span className="element-info-text">{this.state.data.size}</span>
				</div>
			</div>
		</div>
	);
}
