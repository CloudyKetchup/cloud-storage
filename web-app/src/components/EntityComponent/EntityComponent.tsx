import React, { Component } from 'react';

import { Entity } 		from '../../model/entity/Entity';
import App 				from '../../App';
import ContentContainer from '../ContentContainer/ContentContainer';
import EntityContextMenu from "../EntityContextMenu/EntityContextMenu";

export interface EntityProps {
	container 	: ContentContainer
	mainParent 	: App
	handleAction: (action: string) => void
}

export interface EntityState {
	contextMenuShow : boolean,
	contextMenuStyle: {
		top : string,
		left: string
	}
}

export default abstract class EntityComponent<P extends EntityProps> extends Component<P, EntityState> {
	state = {
		contextMenuShow : false,
		contextMenuStyle : {
			top: '',
			left: ''
		}
	};

	contextMenu = (data: Entity, handleAction: (action: string) => void, app : App) => (
		this.state.contextMenuShow
		&&
		<EntityContextMenu
			parent={data}
			action={handleAction}
			onStart={() => app.setState({ elementSelected: data })}
			style={this.state.contextMenuStyle}
		/>
	);

	name = (name: string) =>  name.length > 14 ? `${name.substring(0, 13)}...` : name;
}
