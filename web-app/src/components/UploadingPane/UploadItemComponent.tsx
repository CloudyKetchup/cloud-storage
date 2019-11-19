import React, { Component } from "react";

import { UploadItem }       from '../../model/UploadItem';
import { LinearProgress }   from "@material-ui/core";
import UploadQueue          from "../../utils/UploadQueue";

import './uploading-pane.css';

type IProps = { data : UploadItem };

type IState = { progress : number, canceled : boolean };

export default class UploadItemComponent extends Component<IProps> {
	state : IState = {
		progress : Number(this.props.data.progress),
		canceled : UploadQueue.getInstance().jobCanceled(this.props.data.id)
	};

	timer : number | undefined = undefined;

	componentDidMount = () => {
		this.timer = setInterval(async () => {
			const progress = Number(this.props.data.progress);

			this.setState({ progress : progress });

			if (progress > 99 && this.timer) clearInterval(this.timer);
		}, 100);
	};

	shouldComponentUpdate = (nextProps: Readonly<IProps>, nextState: Readonly<IState>) =>
		this.state !== nextState || this.props !== nextProps;

	componentWillUnmount = () => clearInterval(this.timer);

	name = () => {
		const name = this.props.data.file.name;

		return name.length > 20 ? `${name.substring(0, 20)}...` : name;
	};

	suspend = async () => {
		clearInterval(this.timer);

		UploadQueue.getInstance().suspendUpload(this.props.data);

		this.setState({ canceled : UploadQueue.getInstance().jobCanceled(this.props.data.id) });
	};

	progressStyle = { borderBottomLeftRadius : 5, borderBottomRightRadius : 5 };

	render = () => (
		<div className="upload-item">
			<div>
				<div>
					<span style={{ marginLeft : 10 }}>{this.name()}</span>
				</div>
				{
					this.state.progress < 100
					?
					!this.state.canceled
					&&
					<div style={{marginLeft: "auto", width: 50, textAlign: "center"}}>
						{
							this.state.progress < 100
							&&
							this.state.progress > 0
							&&
                            <div className="upload-item-control-button" onClick={this.suspend}>
                                <i className="fas fa-stop-circle"/>
                            </div>
						}
                    </div>
						:
						<div style={{ marginLeft : "auto", marginRight : 10 }}>
							<i className="fas fa-check"/>
						</div>
				}
			</div>
			<div style={{ height: 2, width: "100%", filter : this.state.canceled ? "grayscale(1)" : "" }}>
				{
					this.state.progress === 0
					&&
					!this.state.canceled
						?
						<LinearProgress color="secondary" style={this.progressStyle}/>
						:
						<LinearProgress
							color="secondary"
							style={this.progressStyle}
							variant="determinate"
							value={this.state.progress}
						/>
				}
			</div>
		</div>
	);
}
