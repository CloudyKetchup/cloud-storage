import React, { Component, SFC } from 'react'

import { Link }       	from "react-router-dom";
import { Entity }     	from "../../model/entity/Entity";
import { API_URL } 		from "../../helpers";

type ContextMenuProps = {
	onStart: () => void,
	action: (action: string) => void
	parent: Entity
	style: any
};

type ContextMenuItemProps = {
	onClick? : (...args: any[]) => any
	icon?	 : JSX.Element
	text	 : string
};

export const ContextMenuItem : SFC<ContextMenuItemProps> = (props : ContextMenuItemProps) => (
	<div onClick={props.onClick}>
		<div className="context-menu-icon">
			{props.icon}
		</div>
		<span>{props.text}</span>
	</div>
);

export default class EntityContextMenu extends Component<ContextMenuProps> {
	state = {
		downloadUrl: `${API_URL}/${this.props.parent.type.toLowerCase()}/${this.props.parent.path.replace(/[\\]/g,"%2F")}/download`
	};

	componentDidMount = () => this.props.onStart();

	render = () => (
		<div className="context-menu" id={`entity-${this.props.parent.id}-context-menu`} style={this.props.style}>
			{this.props.children}
			<div onClick={() => this.props.action("download")}>
				<div className="context-menu-icon">
					<i className="fas fa-download" />
				</div>
				<span>Download</span>
			</div>
			<div onClick={() => this.props.action("move")}>
				<div className="context-menu-icon">
					<i className="fas fa-cut" />
				</div>
				<span>Cut</span>
			</div>
			<div onClick={() => this.props.action("copy")}>
				<div className="context-menu-icon">
					<i className="fas fa-copy" />
				</div>
				<span>Copy</span>
			</div>
			<Link to={`/${this.props.parent.type.toLowerCase()}/${this.props.parent.id}/rename`}>
				<div className="context-menu-icon">
					<i className="fas fa-signature" />
				</div>
				<span>Rename</span>
			</Link>
			<div onClick={() => this.props.action("delete")}>
				<div className="context-menu-icon">
					<i className="fas fa-times" />
				</div>
				<span>Delete</span>
			</div>
			<div onClick={() => this.props.action("trash")}>
				<div className="context-menu-icon">
					<i className="far fa-trash-alt" />
				</div>
				<span>Move to trash</span>
			</div>
			<Link to={`/info?type=${this.props.parent.type.toLowerCase()}&id=${this.props.parent.id}`}>
				<div className="context-menu-icon">
					<i className="fas fa-info" />
				</div>
				<span>Info</span>
			</Link>
		</div>
	);
}