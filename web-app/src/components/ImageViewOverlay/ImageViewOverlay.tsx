import React, { Component, FC }     from 'react';
import {APIHelpers as API, API_URL, ContentHelpers, EntityHelpers} from '../../helpers';
import { FileEntity }               from '../../model/entity/FileEntity';
import CircularProgress             from '@material-ui/core/CircularProgress/CircularProgress';
import { Link, Route, useHistory }  from 'react-router-dom';
import { ElementInfoContainer }     from '../ElementInfoContainer/ElementInfoContainer';

interface ImageViewInterface { id : string }

type IState = {
	data : FileEntity | null,
	loaded : boolean
};

export default class ImageViewOverlay extends Component<ImageViewInterface> {
	state : IState = {
		data : null,
		loaded : false
	};

    imageView    : HTMLElement  | null = null;
    imageDiv     : HTMLElement  | null = null;

	UNSAFE_componentWillMount = async () => this.setState({ data : await API.getFileData(this.props.id) });

    componentDidMount = async () => window.addEventListener("resize", () => this.resizeImageView());

    componentWillUnmount = () => window.removeEventListener("resize", () => this.resizeImageView());

    componentDidUpdate = async () => {
        if (this.imageDiv == null) this.imageDiv = document.getElementById(`image-view-div-${this.props.id}`);
        if (this.imageView == null) this.imageView = document.getElementById(`image-view-${this.props.id}`);

        this.resizeImageView();
    };

    resizeImageView = async (
        maxHeight = 80 / 100 * window.innerHeight + 50,
        maxWidth = 90 / 100 * window.innerWidth
    ) => {
        if (this.imageView) {
            this.imageView.style.maxHeight = this.imageDiv ? `${this.imageDiv.clientHeight + 50}px` : `${maxHeight}px`;
            this.imageView.style.maxWidth = `${maxWidth}px`;
        }
    };
	
    hoverEvent = () => {
        const div = document.getElementById(`image-view-${this.props.id}`);

        if (div !== null) {
            const display = async (value : string) => {
                const controlButtons = document.getElementsByClassName("media-view-control-button");

                if (controlButtons !== null) {
                    Array.prototype.slice.call(controlButtons).forEach(t => (t as HTMLElement).style.display = value);
                }
            };

            div.addEventListener("mouseover", async () => display("unset"));
            div.addEventListener("mouseout", async () => display("none"));
        }
    };

	imagePreloader = () => (
		this.state.data
		&&
        <div key={EntityHelpers.uuidv4()} style={{
            transform: "translateY(400%)",
            margin: "auto",
            background: "white",
            height: "80px",
            width: "80px",
            borderRadius: 5,
        }}>
            <div
                key={this.state.data.id}
                style={{ height: "40px", lineHeight: "110px" }}
            >
                <CircularProgress style={{ color: "#F32C2C" }} />
            </div>
        </div>
	);

    DeleteButton : FC = () => {
        const history = useHistory();

        const redirect = (url: string) => history.push(url);

        return (
            <button
                className="media-view-control-button"
                onClick={async () =>
                    await this.delete()
                    &&
                    this.state.data
                    &&
                    await ContentHelpers.updateContent(this.state.data.parentId)
                    &&
                    redirect("/")
                }
            >
                <i className="far fa-times-circle" />
            </button>
        );
    };

    TrashButton : FC = () => {
        const history = useHistory();

        const redirect = (url: string) => history.push(url);

        return (
            <button
                className="media-view-control-button"
                onClick={async () => 
                    await this.moveToTrash()
                    &&
                    await ContentHelpers.updateTrash()
                    &&
                    this.state.data
                    &&
                    await ContentHelpers.updateContent(this.state.data.parentId)
                    &&
                    redirect("/")
                }
            >
                <i className="fas fa-trash-alt"/>
            </button>
        );
    };

    delete = async () : Promise<Boolean | null> => this.state.data && await API.deleteEntity(this.state.data) === "OK";

    moveToTrash = async () : Promise<Boolean | null> => this.state.data && await API.moveToTrash(this.state.data) === "OK";

	render = () => (
		<div className="media-view-overlay">
			<div className="media-view-container">
				{
                    this.state.data
                    &&
                    [
                        !this.state.loaded
                        &&
                        this.imagePreloader(),
                        <div
                            className="image-view"
                            key={`image-view-${this.props.id}`}
                            id={`image-view-${this.props.id}`}
                            style={{
                                display     : this.state.loaded ? "" : "none",
                                maxHeight   : 80 / 100 * window.innerHeight + 50,
                                maxWidth    : 90 / 100 * window.innerWidth,
                            }}>
                            <div className="media-view-title">
                                <span>{this.state.data ? this.state.data.name : "null"}</span>
                            </div>
                            <div>
                                <img
                                    id={`image-view-div-${this.props.id}`}
                                    key={this.state.data.path}
                                    style={{
                                        display: this.state.loaded ? "unset" : "none",
                                        maxHeight: 80 / 100 * window.innerHeight,
                                    }}
                                    onLoad={() => { this.hoverEvent(); this.setState({ loaded: true }) }}
                                    src={`${API_URL}/file/${this.state.data.id}/image`}
                                    alt="..."
                                />
                            </div>
                            {
                                this.state.loaded
                                &&
                                <div className="media-view-control">
                                    <this.TrashButton/>
                                    <this.DeleteButton/>
                                    <Link to={`/file/image/${this.props.id}/view/info`}>
                                        <button className="media-view-control-button">
                                            <i className="fas fa-info-circle" />
                                        </button>
                                    </Link>
                                </div>
                            }
                        </div>
					]
				}
				<Route path="/:type/image/:id/view/info" render={props => <ElementInfoContainer key={`media-view-info-${this.props.id}`} prevLink={`/file/image/${this.props.id}/view`} {...props}/>}/>
			</div>
            <Link to="/">
                <button className="close-button">
                    <i className="fas fa-times" style={{ fontSize : 25 }}/>
                </button>
            </Link>
		</div>
	);
}
