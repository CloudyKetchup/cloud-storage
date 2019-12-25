import React, { Component } from 'react';

import queryString          from "query-string";
import { Link } 		    from "react-router-dom";
import { FileEntity } 		from '../../model/entity/FileEntity';
import { FolderEntity } 	from '../../model/entity/FolderEntity';
import { APIHelpers as API}	from '../../helpers';
import { Entity } 			from '../../model/entity/Entity';

interface IProps {
	id?         : string
	type?       : string
	prevLink?   : string
	onClose?    : () => void
}

interface IState {  data : Entity | null}

export class ElementInfoContainer extends Component<IProps, IState> {
	state : IState = { data : null };

	componentDidMount() {
		const query = queryString.parse(window.location.search);
		const id = query.id as string || this.props.id;
		const entityType = query.type || this.props.type;

		if (entityType && id) {
			const functionName = `get${entityType[0].toUpperCase() + entityType.slice(1)}Data`;

			if (functionName === "getFileData" || functionName === "getFolderData")
				(API[functionName](id) as Promise<Entity>)
					.then(result => {
						if ((result as FileEntity) !== undefined)
							this.setState({data: result as FileEntity});
						else if ((result as FolderEntity) !== undefined)
							this.setState({data: result as FolderEntity});
					});
		}
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
				{
					<Link
						style={{float: "left"}}
						to={this.props.prevLink ? this.props.prevLink : "/"}>
						<button
							onClick={this.props.onClose}
							className="prev-button"
						>
							<i className="fas fa-chevron-left"/>
						</button>
					</Link>
				}
				<span>{this.state.data && this.state.data.type.toLowerCase()} info</span>
			</div>
			<div className="element-info" style={{
				height: 'calc(100% - 60px)',
				width: '95%',
				bottom: '0px'
			}}>
				{
					this.state.data
					&&
					<div>
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
				}
			</div>
		</div>
	);
}
